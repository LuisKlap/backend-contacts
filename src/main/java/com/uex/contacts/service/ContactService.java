package com.uex.contacts.service;

import com.uex.contacts.dto.contact.ContactRequest;
import com.uex.contacts.dto.contact.ContactResponse;
import com.uex.contacts.entity.Contact;
import com.uex.contacts.entity.User;
import com.uex.contacts.exception.BadRequestException;
import com.uex.contacts.exception.ConflictException;
import com.uex.contacts.exception.ResourceNotFoundException;
import com.uex.contacts.repository.ContactRepository;
import com.uex.contacts.util.CpfValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContactService {
  private final ContactRepository contactRepository;

  @Transactional
  public ContactResponse createContact(User owner, ContactRequest request) {
    Long ownerId = owner.getId();
    String cleanedCpf = CpfValidator.clean(request.cpf());

    if (!CpfValidator.isValid(cleanedCpf)) {
      throw new BadRequestException("CPF inválido");
    }

    if (contactRepository.existsByOwnerIdAndCpf(ownerId, cleanedCpf)) {
      throw new ConflictException("CPF já cadastrado para este usuário");
    }

    Contact contact = Contact.builder()
        .name(request.name())
        .cpf(cleanedCpf)
        .phone(request.phone())
        .cep(request.cep())
        .state(request.state())
        .city(request.city())
        .street(request.street())
        .number(request.number())
        .complement(request.complement())
        .owner(owner)
        .build();

    Contact saved = contactRepository.save(contact);
    return toResponse(saved);
  }

  @Transactional
  public ContactResponse updateContact(Long contactId, User owner, ContactRequest request) {
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

    Contact updated = contactRepository.save(contact);
    return toResponse(updated);
  }

  @Transactional(readOnly = true)
  public Page<ContactResponse> listContacts(User owner, String filter, Pageable pageable) {
    Long ownerId = owner.getId();
    Page<Contact> page;

    if (filter == null || filter.isBlank()) {
      page = contactRepository.findByOwnerId(ownerId, pageable);
    } else {
      String q = filter.trim();

      if (q.chars().anyMatch(Character::isLetter)) {
        page = contactRepository.findByOwnerIdAndNameContainingIgnoreCase(ownerId, q, pageable);
      } else {
        page = contactRepository.findByOwnerIdAndCpfContaining(ownerId, q, pageable);
      }
    }

    return page.map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public ContactResponse getContact(Long contactId, User owner) {
    Contact contact = contactRepository.findById(contactId)
        .orElseThrow(() -> new ResourceNotFoundException("Contato não encontrado"));

    if (!Objects.equals(contact.getOwner().getId(), owner.getId())) {
      throw new ResourceNotFoundException("Contato não encontrado");
    }

    return toResponse(contact);
  }

  @Transactional
  public void deleteContact(Long contactId, User owner) {
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
        lat,
        lng,
        c.getCreatedAt(),
        c.getUpdatedAt());
  }

}
