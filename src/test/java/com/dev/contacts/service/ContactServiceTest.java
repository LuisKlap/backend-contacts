package com.dev.contacts.service;

import com.dev.contacts.dto.address.AddressResponse;
import com.dev.contacts.dto.contact.ContactRequest;
import com.dev.contacts.dto.contact.ContactResponse;
import com.dev.contacts.entity.Contact;
import com.dev.contacts.entity.User;
import com.dev.contacts.exception.BadRequestException;
import com.dev.contacts.exception.ConflictException;
import com.dev.contacts.exception.ExternalServiceException;
import com.dev.contacts.exception.ResourceNotFoundException;
import com.dev.contacts.repository.ContactRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService Tests")
class ContactServiceTest {

  @Mock
  private ContactRepository contactRepository;

  @Mock
  private AddressLookupService addressLookupService;

  @InjectMocks
  private ContactService contactService;

  private User owner;
  private ContactRequest contactRequest;
  private Contact contact;

  @BeforeEach
  void setUp() {
    owner = User.builder()
        .id(1L)
        .fullName("John Doe")
        .email("john@example.com")
        .passwordHash("hashedPassword")
        .build();

    contactRequest = new ContactRequest(
        "Jane Smith",
        "91210822008",
        "11987654321",
        "01310-100",
        "SP",
        "São Paulo",
        "Av. Paulista",
        "1000",
        "Apto 101",
        "Bela Vista");

    contact = Contact.builder()
        .id(1L)
        .name("Jane Smith")
        .cpf("91210822008")
        .phone("11987654321")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Av. Paulista")
        .number("1000")
        .complement("Apto 101")
        .neighborhood("Bela Vista")
        .latitude(new BigDecimal("-23.561684"))
        .longitude(new BigDecimal("-46.655981"))
        .owner(owner)
        .build();
  }

  @Test
  @DisplayName("Deve criar contato com sucesso")
  void shouldCreateContactSuccessfully() {
    AddressResponse geoResponse = new AddressResponse();
    geoResponse.setLatitude(-23.561684);
    geoResponse.setLongitude(-46.655981);

    when(contactRepository.existsByOwnerIdAndCpf(owner.getId(), "91210822008")).thenReturn(false);
    when(addressLookupService.geocodeAddress(anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(geoResponse);
    when(contactRepository.save(any(Contact.class))).thenReturn(contact);

    ContactResponse response = contactService.createContact(owner, contactRequest);

    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("Jane Smith");
    assertThat(response.cpf()).isEqualTo("91210822008");

    verify(contactRepository).existsByOwnerIdAndCpf(owner.getId(), "91210822008");
    verify(addressLookupService).geocodeAddress(anyString(), anyString(), anyString(), anyString(), anyString());
    verify(contactRepository).save(any(Contact.class));
  }

  @Test
  @DisplayName("Deve criar contato mesmo quando geocoding falha")
  void shouldCreateContactWhenGeocodingFails() {
    when(contactRepository.existsByOwnerIdAndCpf(owner.getId(), "91210822008")).thenReturn(false);
    when(addressLookupService.geocodeAddress(anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenThrow(new ExternalServiceException("Geocoding failed"));
    when(contactRepository.save(any(Contact.class))).thenReturn(contact);

    ContactResponse response = contactService.createContact(owner, contactRequest);

    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("Jane Smith");

    verify(contactRepository).save(any(Contact.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar contato com owner nulo")
  void shouldThrowExceptionWhenCreatingContactWithNullOwner() {
    assertThatThrownBy(() -> contactService.createContact(null, contactRequest))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Authenticated user not found. Contact must have an owner.");

    verify(contactRepository, never()).save(any(Contact.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar contato com CPF inválido")
  void shouldThrowExceptionWhenCreatingContactWithInvalidCpf() {
    ContactRequest invalidRequest = new ContactRequest(
        "Jane Smith",
        "111.111.111-11",
        "11987654321",
        "01310-100",
        "SP",
        "São Paulo",
        "Av. Paulista",
        "1000",
        "Apto 101",
        "Bela Vista");

    assertThatThrownBy(() -> contactService.createContact(owner, invalidRequest))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("CPF inválido");

    verify(contactRepository, never()).save(any(Contact.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar contato com CPF já cadastrado")
  void shouldThrowExceptionWhenCreatingContactWithDuplicateCpf() {
    when(contactRepository.existsByOwnerIdAndCpf(owner.getId(), "91210822008")).thenReturn(true);

    assertThatThrownBy(() -> contactService.createContact(owner, contactRequest))
        .isInstanceOf(ConflictException.class)
        .hasMessage("CPF já cadastrado para este usuário");

    verify(contactRepository).existsByOwnerIdAndCpf(owner.getId(), "91210822008");
    verify(contactRepository, never()).save(any(Contact.class));
  }

  @Test
  @DisplayName("Deve atualizar contato com sucesso")
  void shouldUpdateContactSuccessfully() {
    AddressResponse geoResponse = new AddressResponse();
    geoResponse.setLatitude(-23.561684);
    geoResponse.setLongitude(-46.655981);

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    when(addressLookupService.geocodeAddress(anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(geoResponse);
    when(contactRepository.save(any(Contact.class))).thenReturn(contact);

    ContactResponse response = contactService.updateContact(1L, owner, contactRequest);

    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("Jane Smith");

    verify(contactRepository).findById(1L);
    verify(contactRepository).save(any(Contact.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar contato não encontrado")
  void shouldThrowExceptionWhenUpdatingNonExistentContact() {
    when(contactRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> contactService.updateContact(999L, owner, contactRequest))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Contato não encontrado");

    verify(contactRepository).findById(999L);
    verify(contactRepository, never()).save(any(Contact.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao atualizar contato de outro usuário")
  void shouldThrowExceptionWhenUpdatingContactOfAnotherUser() {
    User anotherOwner = User.builder().id(2L).email("another@example.com").build();

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

    assertThatThrownBy(() -> contactService.updateContact(1L, anotherOwner, contactRequest))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Contato não encontrado");

    verify(contactRepository).findById(1L);
    verify(contactRepository, never()).save(any(Contact.class));
  }

  @Test
  @DisplayName("Deve listar contatos com sucesso")
  void shouldListContactsSuccessfully() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> page = new PageImpl<>(Arrays.asList(contact));

    when(contactRepository.findByOwnerId(owner.getId(), pageable)).thenReturn(page);

    Page<ContactResponse> response = contactService.listContacts(owner, null, pageable);

    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(1);
    assertThat(response.getContent().get(0).name()).isEqualTo("Jane Smith");

    verify(contactRepository).findByOwnerId(owner.getId(), pageable);
  }

  @Test
  @DisplayName("Deve listar contatos filtrados por nome")
  void shouldListContactsFilteredByName() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> page = new PageImpl<>(Arrays.asList(contact));

    when(contactRepository.findByOwnerIdAndNameContainingIgnoreCase(owner.getId(), "Jane", pageable))
        .thenReturn(page);

    Page<ContactResponse> response = contactService.listContacts(owner, "Jane", pageable);

    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(1);

    verify(contactRepository).findByOwnerIdAndNameContainingIgnoreCase(owner.getId(), "Jane", pageable);
  }

  @Test
  @DisplayName("Deve listar contatos filtrados por CPF")
  void shouldListContactsFilteredByCpf() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> page = new PageImpl<>(Arrays.asList(contact));

    when(contactRepository.findByOwnerIdAndCpfDigitsContaining(owner.getId(), "123", pageable))
        .thenReturn(page);

    Page<ContactResponse> response = contactService.listContacts(owner, "123", pageable);

    assertThat(response).isNotNull();
    assertThat(response.getContent()).hasSize(1);

    verify(contactRepository).findByOwnerIdAndCpfDigitsContaining(owner.getId(), "123", pageable);
  }

  @Test
  @DisplayName("Deve retornar página vazia quando filtro de CPF tem menos de 3 dígitos")
  void shouldReturnEmptyPageWhenCpfFilterHasLessThan3Digits() {
    Pageable pageable = PageRequest.of(0, 10);

    Page<ContactResponse> response = contactService.listContacts(owner, "12", pageable);

    assertThat(response).isNotNull();
    assertThat(response.getContent()).isEmpty();

    verify(contactRepository, never()).findByOwnerIdAndCpfDigitsContaining(anyLong(), anyString(), any());
  }

  @Test
  @DisplayName("Deve buscar contato por ID com sucesso")
  void shouldGetContactByIdSuccessfully() {
    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

    ContactResponse response = contactService.getContact(1L, owner);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.name()).isEqualTo("Jane Smith");

    verify(contactRepository).findById(1L);
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar contato não encontrado")
  void shouldThrowExceptionWhenGettingNonExistentContact() {
    when(contactRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> contactService.getContact(999L, owner))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Contato não encontrado");

    verify(contactRepository).findById(999L);
  }

  @Test
  @DisplayName("Deve lançar exceção ao buscar contato de outro usuário")
  void shouldThrowExceptionWhenGettingContactOfAnotherUser() {
    User anotherOwner = User.builder().id(2L).email("another@example.com").build();

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

    assertThatThrownBy(() -> contactService.getContact(1L, anotherOwner))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Contato não encontrado");

    verify(contactRepository).findById(1L);
  }

  @Test
  @DisplayName("Deve deletar contato com sucesso")
  void shouldDeleteContactSuccessfully() {
    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

    contactService.deleteContact(1L, owner);

    verify(contactRepository).findById(1L);
    verify(contactRepository).delete(contact);
  }

  @Test
  @DisplayName("Deve lançar exceção ao deletar contato não encontrado")
  void shouldThrowExceptionWhenDeletingNonExistentContact() {
    when(contactRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> contactService.deleteContact(999L, owner))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Contato não encontrado");

    verify(contactRepository).findById(999L);
    verify(contactRepository, never()).delete(any(Contact.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao deletar contato de outro usuário")
  void shouldThrowExceptionWhenDeletingContactOfAnotherUser() {
    User anotherOwner = User.builder().id(2L).email("another@example.com").build();

    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

    assertThatThrownBy(() -> contactService.deleteContact(1L, anotherOwner))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Contato não encontrado");

    verify(contactRepository).findById(1L);
    verify(contactRepository, never()).delete(any(Contact.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao criar contato com owner sem ID")
  void shouldThrowExceptionWhenCreatingContactWithOwnerWithoutId() {
    User ownerWithoutId = User.builder().email("test@example.com").build();

    assertThatThrownBy(() -> contactService.createContact(ownerWithoutId, contactRequest))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Authenticated user not found. Contact must have an owner.");

    verify(contactRepository, never()).save(any(Contact.class));
  }
}
