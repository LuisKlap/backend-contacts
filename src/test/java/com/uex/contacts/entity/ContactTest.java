package com.uex.contacts.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Contact Entity Tests")
class ContactTest {

  private Contact contact;
  private User owner;

  @BeforeEach
  void setUp() {
    owner = User.builder()
        .id(1L)
        .fullName("John Doe")
        .email("john.doe@example.com")
        .passwordHash("hashedPassword123")
        .build();

    contact = Contact.builder()
        .id(1L)
        .name("Jane Smith")
        .cpf("12345678901")
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
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .owner(owner)
        .build();
  }

  @Test
  @DisplayName("Deve criar um contato com builder")
  void shouldCreateContactWithBuilder() {
    assertThat(contact).isNotNull();
    assertThat(contact.getId()).isEqualTo(1L);
    assertThat(contact.getName()).isEqualTo("Jane Smith");
    assertThat(contact.getCpf()).isEqualTo("12345678901");
    assertThat(contact.getPhone()).isEqualTo("11987654321");
    assertThat(contact.getCep()).isEqualTo("01310-100");
    assertThat(contact.getState()).isEqualTo("SP");
    assertThat(contact.getCity()).isEqualTo("São Paulo");
    assertThat(contact.getStreet()).isEqualTo("Av. Paulista");
    assertThat(contact.getNumber()).isEqualTo("1000");
    assertThat(contact.getComplement()).isEqualTo("Apto 101");
    assertThat(contact.getNeighborhood()).isEqualTo("Bela Vista");
    assertThat(contact.getLatitude()).isEqualByComparingTo(new BigDecimal("-23.561684"));
    assertThat(contact.getLongitude()).isEqualByComparingTo(new BigDecimal("-46.655981"));
    assertThat(contact.getOwner()).isEqualTo(owner);
  }

  @Test
  @DisplayName("Deve criar contato com construtor padrão")
  void shouldCreateContactWithDefaultConstructor() {
    Contact newContact = new Contact();
    newContact.setId(2L);
    newContact.setName("Another Contact");
    newContact.setCpf("98765432100");
    newContact.setPhone("11912345678");
    newContact.setCep("02310-100");
    newContact.setState("SP");
    newContact.setCity("São Paulo");
    newContact.setStreet("Rua Exemplo");
    newContact.setNumber("200");
    newContact.setNeighborhood("Centro");
    newContact.setOwner(owner);

    assertThat(newContact.getId()).isEqualTo(2L);
    assertThat(newContact.getName()).isEqualTo("Another Contact");
    assertThat(newContact.getCpf()).isEqualTo("98765432100");
    assertThat(newContact.getOwner()).isEqualTo(owner);
  }

  @Test
  @DisplayName("Deve criar contato sem complemento")
  void shouldCreateContactWithoutComplement() {
    Contact contactWithoutComplement = Contact.builder()
        .id(3L)
        .name("No Complement Contact")
        .cpf("11111111111")
        .phone("11999999999")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Av. Paulista")
        .number("1500")
        .neighborhood("Bela Vista")
        .owner(owner)
        .build();

    assertThat(contactWithoutComplement.getComplement()).isNull();
  }

  @Test
  @DisplayName("Deve criar contato sem coordenadas geográficas")
  void shouldCreateContactWithoutCoordinates() {
    Contact contactWithoutCoords = Contact.builder()
        .id(4L)
        .name("No Coordinates Contact")
        .cpf("22222222222")
        .phone("11988888888")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Av. Paulista")
        .number("2000")
        .neighborhood("Bela Vista")
        .owner(owner)
        .build();

    assertThat(contactWithoutCoords.getLatitude()).isNull();
    assertThat(contactWithoutCoords.getLongitude()).isNull();
  }

  @Test
  @DisplayName("Deve associar contato com owner")
  void shouldAssociateContactWithOwner() {
    assertThat(contact.getOwner()).isNotNull();
    assertThat(contact.getOwner()).isEqualTo(owner);
    assertThat(contact.getOwner().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Deve validar equals e hashCode baseado apenas no id")
  void shouldValidateEqualsAndHashCodeBasedOnId() {
    Contact contact1 = Contact.builder().id(1L).name("Contact 1").build();
    Contact contact2 = Contact.builder().id(1L).name("Contact 2").build();
    Contact contact3 = Contact.builder().id(2L).name("Contact 1").build();

    assertThat(contact1).isEqualTo(contact2);
    assertThat(contact1).isNotEqualTo(contact3);
    assertThat(contact1.hashCode()).isEqualTo(contact2.hashCode());
    assertThat(contact1.hashCode()).isNotEqualTo(contact3.hashCode());
  }

  @Test
  @DisplayName("Deve gerar toString excluindo owner")
  void shouldGenerateToStringExcludingOwner() {
    String toString = contact.toString();

    assertThat(toString).contains("Jane Smith");
    assertThat(toString).contains("12345678901");
    assertThat(toString).doesNotContain("owner");
  }

  @Test
  @DisplayName("Deve atualizar campos do contato")
  void shouldUpdateContactFields() {
    contact.setName("Updated Name");
    contact.setCpf("99999999999");
    contact.setPhone("11955555555");
    contact.setCity("Rio de Janeiro");
    contact.setState("RJ");

    assertThat(contact.getName()).isEqualTo("Updated Name");
    assertThat(contact.getCpf()).isEqualTo("99999999999");
    assertThat(contact.getPhone()).isEqualTo("11955555555");
    assertThat(contact.getCity()).isEqualTo("Rio de Janeiro");
    assertThat(contact.getState()).isEqualTo("RJ");
  }

  @Test
  @DisplayName("Deve manter timestamps de criação e atualização")
  void shouldMaintainTimestamps() {
    assertThat(contact.getCreatedAt()).isNotNull();
    assertThat(contact.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Deve criar contato com todos os campos obrigatórios")
  void shouldCreateContactWithAllRequiredFields() {
    Contact minimalContact = Contact.builder()
        .name("Minimal Contact")
        .cpf("33333333333")
        .phone("11966666666")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Rua Teste")
        .number("100")
        .neighborhood("Centro")
        .owner(owner)
        .build();

    assertThat(minimalContact.getName()).isEqualTo("Minimal Contact");
    assertThat(minimalContact.getCpf()).isEqualTo("33333333333");
    assertThat(minimalContact.getPhone()).isEqualTo("11966666666");
    assertThat(minimalContact.getCep()).isEqualTo("01310-100");
    assertThat(minimalContact.getState()).isEqualTo("SP");
    assertThat(minimalContact.getCity()).isEqualTo("São Paulo");
    assertThat(minimalContact.getStreet()).isEqualTo("Rua Teste");
    assertThat(minimalContact.getNumber()).isEqualTo("100");
    assertThat(minimalContact.getNeighborhood()).isEqualTo("Centro");
    assertThat(minimalContact.getOwner()).isEqualTo(owner);
  }
}
