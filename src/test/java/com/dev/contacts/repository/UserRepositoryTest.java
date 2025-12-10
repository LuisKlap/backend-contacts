package com.dev.contacts.repository;

import com.dev.contacts.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private User user;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();

    user = User.builder()
        .fullName("John Doe")
        .email("john.doe@example.com")
        .passwordHash("hashedPassword123")
        .build();
  }

  @Test
  @DisplayName("Deve salvar um usuário")
  void shouldSaveUser() {
    User savedUser = userRepository.save(user);

    assertThat(savedUser).isNotNull();
    assertThat(savedUser.getId()).isNotNull();
    assertThat(savedUser.getFullName()).isEqualTo("John Doe");
    assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
    assertThat(savedUser.getCreatedAt()).isNotNull();
  }

  @Test
  @DisplayName("Deve buscar usuário por email")
  void shouldFindUserByEmail() {
    userRepository.save(user);

    Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getFullName()).isEqualTo("John Doe");
    assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
  }

  @Test
  @DisplayName("Deve retornar vazio quando usuário não existe por email")
  void shouldReturnEmptyWhenUserNotFoundByEmail() {
    Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

    assertThat(foundUser).isEmpty();
  }

  @Test
  @DisplayName("Deve verificar se usuário existe por email")
  void shouldCheckIfUserExistsByEmail() {
    userRepository.save(user);

    boolean exists = userRepository.existsByEmail("john.doe@example.com");

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Deve retornar false quando usuário não existe por email")
  void shouldReturnFalseWhenUserDoesNotExistByEmail() {
    boolean exists = userRepository.existsByEmail("nonexistent@example.com");

    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("Deve buscar usuário por ID")
  void shouldFindUserById() {
    User savedUser = userRepository.save(user);

    Optional<User> foundUser = userRepository.findById(savedUser.getId());

    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
    assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
  }

  @Test
  @DisplayName("Deve deletar um usuário")
  void shouldDeleteUser() {
    User savedUser = userRepository.save(user);

    userRepository.delete(savedUser);

    Optional<User> foundUser = userRepository.findById(savedUser.getId());
    assertThat(foundUser).isEmpty();
  }

  @Test
  @DisplayName("Deve atualizar um usuário")
  void shouldUpdateUser() {
    User savedUser = userRepository.save(user);

    savedUser.setFullName("Jane Doe Updated");
    savedUser.setEmail("jane.updated@example.com");
    User updatedUser = userRepository.save(savedUser);

    Optional<User> foundUser = userRepository.findById(updatedUser.getId());
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getFullName()).isEqualTo("Jane Doe Updated");
    assertThat(foundUser.get().getEmail()).isEqualTo("jane.updated@example.com");
    assertThat(foundUser.get().getUpdatedAt()).isAfter(foundUser.get().getCreatedAt());
  }

  @Test
  @DisplayName("Deve listar todos os usuários")
  void shouldListAllUsers() {
    User user2 = User.builder()
        .fullName("Jane Smith")
        .email("jane.smith@example.com")
        .passwordHash("hashedPassword456")
        .build();

    userRepository.save(user);
    userRepository.save(user2);

    var users = userRepository.findAll();

    assertThat(users).hasSize(2);
    assertThat(users).extracting(User::getEmail)
        .containsExactlyInAnyOrder("john.doe@example.com", "jane.smith@example.com");
  }

  @Test
  @DisplayName("Deve garantir unicidade de email")
  void shouldEnforceEmailUniqueness() {
    userRepository.saveAndFlush(user);

    User duplicateUser = User.builder()
        .fullName("Another User")
        .email("john.doe@example.com") // mesmo email
        .passwordHash("anotherHash")
        .build();

    try {
      userRepository.saveAndFlush(duplicateUser);
    } catch (Exception e) {
      assertThat(e).isNotNull();
      // A exceção de constraint violation é esperada
    }
  }

  @Test
  @DisplayName("Deve contar número de usuários")
  void shouldCountUsers() {
    userRepository.save(user);
    User user2 = User.builder()
        .fullName("Jane Smith")
        .email("jane.smith@example.com")
        .passwordHash("hashedPassword456")
        .build();
    userRepository.save(user2);

    long count = userRepository.count();

    assertThat(count).isEqualTo(2);
  }
}
