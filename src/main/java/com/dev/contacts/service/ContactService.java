package com.dev.contacts.service;

import com.dev.contacts.dto.contact.ContactRequest;
import com.dev.contacts.dto.contact.ContactResponse;
import com.dev.contacts.entity.Contact;
import com.dev.contacts.entity.User;
import com.dev.contacts.exception.BadRequestException;
import com.dev.contacts.exception.ConflictException;
import com.dev.contacts.exception.ExternalServiceException;
import com.dev.contacts.exception.ResourceNotFoundException;
import com.dev.contacts.dto.address.AddressResponse;
import com.dev.contacts.repository.ContactRepository;
import com.dev.contacts.util.CpfValidator;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContactService {

  private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

  private final ContactRepository contactRepository;
  private final AddressLookupService addressLookupService;

  @Transactional
  public ContactResponse createContact(User owner, ContactRequest request) {

    if (owner == null || owner.getId() == null) {
      throw new BadRequestException("Authenticated user not found. Contact must have an owner.");
    }

    Long ownerId = owner.getId();
    String cleanedCpf = CpfValidator.clean(request.cpf());

    if (!CpfValidator.isValid(cleanedCpf)) {
      throw new BadRequestException("CPF inválido");
    }

    if (contactRepository.existsByOwnerIdAndCpf(ownerId, cleanedCpf)) {
      throw new ConflictException("CPF já cadastrado para este usuário");
    }

    Contact.ContactBuilder builder = Contact.builder()
        .name(request.name())
        .cpf(cleanedCpf)
        .phone(request.phone())
        .cep(request.cep())
        .state(request.state())
        .city(request.city())
        .street(request.street())
        .number(request.number())
        .complement(request.complement())
        .neighborhood(request.neighborhood())
        .owner(owner);

    // Tenta buscar latitude/longitude via Google Geocoding
    try {
      AddressResponse geoResult = addressLookupService.geocodeAddress(
          request.street(),
          request.number(),
          request.city(),
          request.state(),
          request.cep());

      if (geoResult.getLatitude() != null && geoResult.getLongitude() != null) {
        builder.latitude(BigDecimal.valueOf(geoResult.getLatitude()).setScale(8, RoundingMode.HALF_UP));
        builder.longitude(BigDecimal.valueOf(geoResult.getLongitude()).setScale(8, RoundingMode.HALF_UP));
      }
    } catch (ExternalServiceException ex) {
      logger.warn("Não foi possível obter coordenadas para o endereço: {}", ex.getMessage());
    }

    Contact contact = builder.build();

    Contact saved = contactRepository.save(contact);
    return toResponse(saved);
  }

  @Transactional
  public ContactResponse updateContact(Long contactId, User owner, ContactRequest request) {

    if (owner == null || owner.getId() == null) {
      throw new BadRequestException("Authenticated user not found. Contact must have an owner.");
    }

    Contact contact = contactRepository.findById(contactId)
        .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));

    if (!Objects.equals(contact.getOwner().getId(), owner.getId())) {
      throw new ResourceNotFoundException("Contato não encontrado");
    }

    String cleanedCpf = CpfValidator.clean(request.cpf());
    if (!CpfValidator.isValid(cleanedCpf)) {
      throw new BadRequestException("CPF inválido");
    }

    if (!Objects.equals(cleanedCpf, contact.getCpf())
        && contactRepository.existsByOwnerIdAndCpf(owner.getId(), cleanedCpf)) {
      throw new ConflictException("CPF já cadastrado para este usuário");
    }

    contact.setName(request.name());
    contact.setCpf(cleanedCpf);
    contact.setPhone(request.phone());
    contact.setCep(request.cep());
    contact.setState(request.state());
    contact.setCity(request.city());
    contact.setStreet(request.street());
    contact.setNumber(request.number());
    contact.setComplement(request.complement());
    contact.setNeighborhood(request.neighborhood());

    try {
      if (hasEnoughForGeocode(request)) {
        AddressResponse geoResult = addressLookupService.geocodeAddress(
            request.street(),
            request.number(),
            request.city(),
            request.state(),
            request.cep());

        if (geoResult.getLatitude() != null && geoResult.getLongitude() != null) {
          contact.setLatitude(BigDecimal.valueOf(geoResult.getLatitude()).setScale(8, RoundingMode.HALF_UP));
          contact.setLongitude(BigDecimal.valueOf(geoResult.getLongitude()).setScale(8, RoundingMode.HALF_UP));
        }
      }
    } catch (ExternalServiceException ex) {
      logger.warn("Não foi possível obter coordenadas para o endereço: {}", ex.getMessage());
    }

    Contact updated = contactRepository.save(contact);
    return toResponse(updated);
  }

  @Transactional(readOnly = true)
  public Page<ContactResponse> listContacts(User owner, String filter, Pageable pageable) {
    if (owner == null || owner.getId() == null) {
      throw new BadRequestException("Authenticated user not found.");
    }

    Long ownerId = owner.getId();
    Page<Contact> page;

    if (filter == null || filter.isBlank()) {
      page = contactRepository.findByOwnerId(ownerId, pageable);
    } else {
      String q = filter.trim();

      boolean hasLetter = q.chars().anyMatch(Character::isLetter);

      if (hasLetter) {
        page = contactRepository.findByOwnerIdAndNameContainingIgnoreCase(ownerId, q, pageable);
      } else {
        String digits = q.replaceAll("\\D", "");
        if (digits.length() < 3) {
          return Page.empty(pageable);
        }
        page = contactRepository.findByOwnerIdAndCpfDigitsContaining(ownerId, digits, pageable);
      }
    }

    return page.map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public ContactResponse getContact(Long contactId, User owner) {

    if (owner == null || owner.getId() == null) {
      throw new BadRequestException("Authenticated user not found.");
    }

    Contact contact = contactRepository.findById(contactId)
        .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));

    if (!Objects.equals(contact.getOwner().getId(), owner.getId())) {
      throw new ResourceNotFoundException("Contato não encontrado");
    }

    return toResponse(contact);
  }

  @Transactional
  public void deleteContact(Long contactId, User owner) {

    if (owner == null || owner.getId() == null) {
      throw new BadRequestException("Authenticated user not found.");
    }

    Contact contact = contactRepository.findById(contactId)
        .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));

    if (!Objects.equals(contact.getOwner().getId(), owner.getId())) {
      throw new ResourceNotFoundException("Contato não encontrado");
    }

    contactRepository.delete(contact);
  }

  private ContactResponse toResponse(Contact c) {
    Double lat = c.getLatitude() != null ? c.getLatitude().doubleValue() : null;
    Double lng = c.getLongitude() != null ? c.getLongitude().doubleValue() : null;

    return new ContactResponse(
        c.getId(),
        c.getName(),
        c.getCpf(),
        c.getPhone(),
        c.getCep(),
        c.getState(),
        c.getCity(),
        c.getStreet(),
        c.getNumber(),
        c.getComplement(),
        c.getNeighborhood(),
        lat,
        lng,
        c.getCreatedAt(),
        c.getUpdatedAt());
  }

  private boolean hasEnoughForGeocode(ContactRequest request) {
    return (request.street() != null && !request.street().isBlank())
        && (request.city() != null && !request.city().isBlank())
        && (request.state() != null && !request.state().isBlank());
  }
}
