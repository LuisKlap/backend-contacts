package com.dev.contacts.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Entity Tests")
class UserTest {

  private User user;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .id(1L)
        .fullName("John Doe")
        .email("john.doe@example.com")
        .passwordHash("hashedPassword123")
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .contacts(new HashSet<>())
        .build();
  }

  @Test
  @DisplayName("Deve criar um usuário com builder")
  void shouldCreateUserWithBuilder() {
    assertThat(user).isNotNull();
    assertThat(user.getId()).isEqualTo(1L);
    assertThat(user.getFullName()).isEqualTo("John Doe");
    assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    assertThat(user.getPasswordHash()).isEqualTo("hashedPassword123");
    assertThat(user.getContacts()).isNotNull().isEmpty();
  }

  @Test
  @DisplayName("Deve adicionar contato ao usuário")
  void shouldAddContactToUser() {
    Contact contact = Contact.builder()
        .id(1L)
        .name("Jane Smith")
        .cpf("12345678901")
        .phone("11987654321")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Av. Paulista")
        .number("1000")
        .neighborhood("Bela Vista")
        .build();

    user.addContact(contact);

    assertThat(user.getContacts()).hasSize(1).contains(contact);
    assertThat(contact.getOwner()).isEqualTo(user);
  }

  @Test
  @DisplayName("Deve remover contato do usuário")
  void shouldRemoveContactFromUser() {
    Contact contact = Contact.builder()
        .id(1L)
        .name("Jane Smith")
        .cpf("12345678901")
        .phone("11987654321")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Av. Paulista")
        .number("1000")
        .neighborhood("Bela Vista")
        .build();

    user.addContact(contact);
    assertThat(user.getContacts()).hasSize(1);

    user.removeContact(contact);

    assertThat(user.getContacts()).isEmpty();
    assertThat(contact.getOwner()).isNull();
  }

  @Test
  @DisplayName("Deve retornar email como username")
  void shouldReturnEmailAsUsername() {
    assertThat(user.getUsername()).isEqualTo("john.doe@example.com");
  }

  @Test
  @DisplayName("Deve retornar passwordHash como password")
  void shouldReturnPasswordHashAsPassword() {
    assertThat(user.getPassword()).isEqualTo("hashedPassword123");
  }

  @Test
  @DisplayName("Deve retornar authorities vazio")
  void shouldReturnEmptyAuthorities() {
    assertThat(user.getAuthorities()).isEmpty();
  }

  @Test
  @DisplayName("Deve retornar conta não expirada")
  void shouldReturnAccountNonExpired() {
    assertThat(user.isAccountNonExpired()).isTrue();
  }

  @Test
  @DisplayName("Deve retornar conta não bloqueada")
  void shouldReturnAccountNonLocked() {
    assertThat(user.isAccountNonLocked()).isTrue();
  }

  @Test
  @DisplayName("Deve retornar credenciais não expiradas")
  void shouldReturnCredentialsNonExpired() {
    assertThat(user.isCredentialsNonExpired()).isTrue();
  }

  @Test
  @DisplayName("Deve retornar conta habilitada")
  void shouldReturnEnabled() {
    assertThat(user.isEnabled()).isTrue();
  }

  @Test
  @DisplayName("Deve criar usuário com construtor padrão")
  void shouldCreateUserWithDefaultConstructor() {
    User newUser = new User();
    newUser.setId(2L);
    newUser.setFullName("Jane Doe");
    newUser.setEmail("jane.doe@example.com");
    newUser.setPasswordHash("anotherHash");

    assertThat(newUser.getId()).isEqualTo(2L);
    assertThat(newUser.getFullName()).isEqualTo("Jane Doe");
    assertThat(newUser.getEmail()).isEqualTo("jane.doe@example.com");
    assertThat(newUser.getPasswordHash()).isEqualTo("anotherHash");
  }

  @Test
  @DisplayName("Deve adicionar múltiplos contatos ao usuário")
  void shouldAddMultipleContactsToUser() {
    Contact contact1 = Contact.builder()
        .id(1L)
        .name("Contact 1")
        .cpf("11111111111")
        .phone("11111111111")
        .cep("01310-100")
        .state("SP")
        .city("São Paulo")
        .street("Rua A")
        .number("100")
        .neighborhood("Centro")
        .build();

    Contact contact2 = Contact.builder()
        .id(2L)
        .name("Contact 2")
        .cpf("22222222222")
        .phone("22222222222")
        .cep("01310-200")
        .state("SP")
        .city("São Paulo")
        .street("Rua B")
        .number("200")
        .neighborhood("Centro")
        .build();

    user.addContact(contact1);
    user.addContact(contact2);

    assertThat(user.getContacts()).hasSize(2).contains(contact1, contact2);
    assertThat(contact1.getOwner()).isEqualTo(user);
    assertThat(contact2.getOwner()).isEqualTo(user);
  }
}
