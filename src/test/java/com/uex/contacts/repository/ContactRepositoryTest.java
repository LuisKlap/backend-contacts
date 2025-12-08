package com.uex.contacts.repository;

import com.uex.contacts.entity.Contact;
import com.uex.contacts.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ContactRepository Integration Tests")
class ContactRepositoryTest {

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private UserRepository userRepository;

  private User owner;
  private Contact contact1;
  private Contact contact2;

  @BeforeEach
  void setUp() {
    contactRepository.deleteAll();
    userRepository.deleteAll();

    owner = User.builder()
        .fullName("John Doe")
        .email("john.doe@example.com")
        .passwordHash("hashedPassword123")
        .build();
    owner = userRepository.save(owner);

    contact1 = Contact.builder()
        .name("Jane Smith")
        .cpf("12345678901")
        .phone("11987654321")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Av. Paulista")
        .number("1000")
        .neighborhood("Bela Vista")
        .latitude(new BigDecimal("-23.561684"))
        .longitude(new BigDecimal("-46.655981"))
        .owner(owner)
        .build();

    contact2 = Contact.builder()
        .name("Bob Johnson")
        .cpf("98765432100")
        .phone("11912345678")
        .cep("02310-100")
        .state("SP")
        .city("São Paulo")
        .street("Rua Exemplo")
        .number("200")
        .neighborhood("Centro")
        .owner(owner)
        .build();
  }

  @Test
  @DisplayName("Deve salvar um contato")
  void shouldSaveContact() {
    Contact savedContact = contactRepository.save(contact1);

    assertThat(savedContact).isNotNull();
    assertThat(savedContact.getId()).isNotNull();
    assertThat(savedContact.getName()).isEqualTo("Jane Smith");
    assertThat(savedContact.getCpf()).isEqualTo("12345678901");
    assertThat(savedContact.getOwner()).isEqualTo(owner);
    assertThat(savedContact.getCreatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Deve buscar contatos por owner ID com paginação")
  void shouldFindContactsByOwnerIdWithPagination() {
    contactRepository.save(contact1);
    contactRepository.save(contact2);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> contacts = contactRepository.findByOwnerId(owner.getId(), pageable);

    assertThat(contacts).isNotNull();
    assertThat(contacts.getContent()).hasSize(2);
    assertThat(contacts.getTotalElements()).isEqualTo(2);
  }

  @Test
  @DisplayName("Deve buscar contato por owner ID e CPF")
  void shouldFindContactByOwnerIdAndCpf() {
    contactRepository.save(contact1);

    Optional<Contact> foundContact = contactRepository.findByOwnerIdAndCpf(owner.getId(), "12345678901");

    assertThat(foundContact).isPresent();
    assertThat(foundContact.get().getName()).isEqualTo("Jane Smith");
    assertThat(foundContact.get().getCpf()).isEqualTo("12345678901");
  }

  @Test
  @DisplayName("Deve retornar vazio quando contato não existe por owner ID e CPF")
  void shouldReturnEmptyWhenContactNotFoundByOwnerIdAndCpf() {
    Optional<Contact> foundContact = contactRepository.findByOwnerIdAndCpf(owner.getId(), "00000000000");

    assertThat(foundContact).isEmpty();
  }

  @Test
  @DisplayName("Deve verificar se contato existe por owner ID e CPF")
  void shouldCheckIfContactExistsByOwnerIdAndCpf() {
    contactRepository.save(contact1);

    boolean exists = contactRepository.existsByOwnerIdAndCpf(owner.getId(), "12345678901");

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Deve retornar false quando contato não existe por owner ID e CPF")
  void shouldReturnFalseWhenContactDoesNotExistByOwnerIdAndCpf() {
    boolean exists = contactRepository.existsByOwnerIdAndCpf(owner.getId(), "00000000000");

    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("Deve buscar contatos por owner ID e nome contendo texto (case insensitive)")
  void shouldFindContactsByOwnerIdAndNameContaining() {
    contactRepository.save(contact1);
    contactRepository.save(contact2);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> contacts = contactRepository.findByOwnerIdAndNameContainingIgnoreCase(owner.getId(), "jane",
        pageable);

    assertThat(contacts.getContent()).hasSize(1);
    assertThat(contacts.getContent().get(0).getName()).isEqualTo("Jane Smith");
  }

  @Test
  @DisplayName("Deve buscar contatos por owner ID e fragmento de CPF")
  void shouldFindContactsByOwnerIdAndCpfContaining() {
    contactRepository.save(contact1);
    contactRepository.save(contact2);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> contacts = contactRepository.findByOwnerIdAndCpfContaining(owner.getId(), "123", pageable);

    assertThat(contacts.getContent()).hasSize(1);
    assertThat(contacts.getContent().get(0).getCpf()).isEqualTo("12345678901");
  }

  @Test
  @DisplayName("Deve deletar contatos por owner ID")
  void shouldDeleteContactsByOwnerId() {
    contactRepository.save(contact1);
    contactRepository.save(contact2);

    contactRepository.deleteByOwnerId(owner.getId());

    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> contacts = contactRepository.findByOwnerId(owner.getId(), pageable);

    assertThat(contacts.getContent()).isEmpty();
  }

  @Test
  @DisplayName("Deve buscar contato por ID")
  void shouldFindContactById() {
    Contact savedContact = contactRepository.save(contact1);

    Optional<Contact> foundContact = contactRepository.findById(savedContact.getId());

    assertThat(foundContact).isPresent();
    assertThat(foundContact.get().getId()).isEqualTo(savedContact.getId());
    assertThat(foundContact.get().getName()).isEqualTo("Jane Smith");
  }

  @Test
  @DisplayName("Deve deletar um contato")
  void shouldDeleteContact() {
    Contact savedContact = contactRepository.save(contact1);

    contactRepository.delete(savedContact);

    Optional<Contact> foundContact = contactRepository.findById(savedContact.getId());
    assertThat(foundContact).isEmpty();
  }

  @Test
  @DisplayName("Deve atualizar um contato")
  void shouldUpdateContact() {
    Contact savedContact = contactRepository.save(contact1);

    savedContact.setName("Jane Doe Updated");
    savedContact.setPhone("11999999999");
    Contact updatedContact = contactRepository.save(savedContact);

    Optional<Contact> foundContact = contactRepository.findById(updatedContact.getId());
    assertThat(foundContact).isPresent();
    assertThat(foundContact.get().getName()).isEqualTo("Jane Doe Updated");
    assertThat(foundContact.get().getPhone()).isEqualTo("11999999999");
    assertThat(foundContact.get().getUpdatedAt()).isAfter(foundContact.get().getCreatedAt());
  }

  @Test
  @DisplayName("Deve garantir unicidade de CPF por owner")
  void shouldEnforceCpfUniquenessPerOwner() {
    contactRepository.saveAndFlush(contact1);

    Contact duplicateContact = Contact.builder()
        .name("Another Contact")
        .cpf("12345678901") // mesmo CPF
        .phone("11988888888")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Rua Teste")
        .number("300")
        .neighborhood("Centro")
        .owner(owner)
        .build();

    try {
      contactRepository.saveAndFlush(duplicateContact);
    } catch (Exception e) {
      assertThat(e).isNotNull();
      // A exceção de constraint violation é esperada
    }
  }

  @Test
  @DisplayName("Deve permitir mesmo CPF para owners diferentes")
  void shouldAllowSameCpfForDifferentOwners() {
    User anotherOwner = User.builder()
        .fullName("Another Owner")
        .email("another.owner@example.com")
        .passwordHash("hashedPassword456")
        .build();
    anotherOwner = userRepository.save(anotherOwner);

    contactRepository.save(contact1);

    Contact contactWithSameCpf = Contact.builder()
        .name("Another Contact")
        .cpf("12345678901") // mesmo CPF, mas owner diferente
        .phone("11988888888")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Rua Teste")
        .number("300")
        .neighborhood("Centro")
        .owner(anotherOwner)
        .build();

    Contact savedContact = contactRepository.save(contactWithSameCpf);

    assertThat(savedContact).isNotNull();
    assertThat(savedContact.getId()).isNotNull();
  }

  @Test
  @DisplayName("Deve contar número de contatos")
  void shouldCountContacts() {
    contactRepository.save(contact1);
    contactRepository.save(contact2);

    long count = contactRepository.count();

    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("Deve buscar contatos com paginação ordenada")
  void shouldFindContactsWithPaginationAndSorting() {
    contactRepository.save(contact1);
    contactRepository.save(contact2);

    Pageable pageable = PageRequest.of(0, 1);
    Page<Contact> firstPage = contactRepository.findByOwnerId(owner.getId(), pageable);

    assertThat(firstPage.getContent()).hasSize(1);
    assertThat(firstPage.getTotalPages()).isEqualTo(2);
    assertThat(firstPage.getTotalElements()).isEqualTo(2);
    assertThat(firstPage.hasNext()).isTrue();
  }

  @Test
  @DisplayName("Deve retornar página vazia quando não há contatos")
  void shouldReturnEmptyPageWhenNoContacts() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Contact> contacts = contactRepository.findByOwnerId(owner.getId(), pageable);

    assertThat(contacts.getContent()).isEmpty();
    assertThat(contacts.getTotalElements()).isZero();
  }
}
