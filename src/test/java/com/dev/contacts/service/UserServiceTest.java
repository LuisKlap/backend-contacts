package com.dev.contacts.service;

import com.dev.contacts.dto.user.UpdateUserRequest;
import com.dev.contacts.dto.user.UserResponse;
import com.dev.contacts.entity.User;
import com.dev.contacts.exception.ConflictException;
import com.dev.contacts.exception.InvalidCredentialsException;
import com.dev.contacts.exception.ResourceNotFoundException;
import com.dev.contacts.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private UserService userService;

  private User user;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .id(1L)
        .fullName("John Doe")
        .email("john@example.com")
        .passwordHash("hashedPassword")
        .createdAt(OffsetDateTime.now())
        .build();

    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("Deve carregar usuário por email")
  void shouldLoadUserByUsername() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

    UserDetails userDetails = userService.loadUserByUsername("john@example.com");

    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo("john@example.com");
    verify(userRepository).findByEmail("john@example.com");
  }

  @Test
  @DisplayName("Deve lançar exceção quando usuário não é encontrado")
  void shouldThrowExceptionWhenUserNotFoundByUsername() {
    when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.loadUserByUsername("notfound@example.com"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("Usuário não encontrado");

    verify(userRepository).findByEmail("notfound@example.com");
  }

  @Test
  @DisplayName("Deve buscar usuário por ID")
  void shouldFindUserById() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    UserResponse response = userService.findById(1L);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.fullName()).isEqualTo("John Doe");
    assertThat(response.email()).isEqualTo("john@example.com");

    verify(userRepository).findById(1L);
  }

  @Test
  @DisplayName("Deve lançar exceção quando usuário não é encontrado por ID")
  void shouldThrowExceptionWhenUserNotFoundById() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Usuário não encontrado");

    verify(userRepository).findById(999L);
  }

  @Test
  @DisplayName("Deve buscar entidade de usuário por email")
  void shouldFindEntityByEmail() {
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

    Optional<User> result = userService.findEntityByEmail("john@example.com");

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(user);
    verify(userRepository).findByEmail("john@example.com");
  }

  @Test
  @DisplayName("Deve buscar usuário atual autenticado")
  void shouldFindCurrentUser() {
    UserDetails userDetails = org.springframework.security.core.userdetails.User
        .withUsername("john@example.com")
        .password("password")
        .authorities("ROLE_USER")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

    UserResponse response = userService.findCurrentUser();

    assertThat(response).isNotNull();
    assertThat(response.email()).isEqualTo("john@example.com");
    verify(userRepository).findByEmail("john@example.com");
  }

  @Test
  @DisplayName("Deve atualizar usuário atual sem alterar senha")
  void shouldUpdateCurrentUserWithoutPasswordChange() {
    UpdateUserRequest request = new UpdateUserRequest("Jane Doe", "jane@example.com", null, null);

    UserDetails userDetails = org.springframework.security.core.userdetails.User
        .withUsername("john@example.com")
        .password("password")
        .authorities("ROLE_USER")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserResponse response = userService.updateCurrentUser(request);

    assertThat(response).isNotNull();
    verify(userRepository).findByEmail("john@example.com");
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder, never()).encode(anyString());
  }

  @Test
  @DisplayName("Deve atualizar usuário atual com alteração de senha")
  void shouldUpdateCurrentUserWithPasswordChange() {
    UpdateUserRequest request = new UpdateUserRequest("John Doe", "john@example.com", "oldPassword", "newPassword");

    UserDetails userDetails = org.springframework.security.core.userdetails.User
        .withUsername("john@example.com")
        .password("password")
        .authorities("ROLE_USER")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    UserResponse response = userService.updateCurrentUser(request);

    assertThat(response).isNotNull();
    verify(userRepository).findByEmail("john@example.com");
    verify(passwordEncoder, atLeastOnce()).matches(anyString(), anyString());
    verify(passwordEncoder, atLeastOnce()).encode(anyString());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar alterar senha com senha atual inválida")
  void shouldThrowExceptionWhenChangingPasswordWithInvalidCurrentPassword() {
    UpdateUserRequest request = new UpdateUserRequest("John Doe", "john@example.com", "wrongPassword", "newPassword");

    UserDetails userDetails = org.springframework.security.core.userdetails.User
        .withUsername("john@example.com")
        .password("password")
        .authorities("ROLE_USER")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

    assertThatThrownBy(() -> userService.updateCurrentUser(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Senha atual inválida");

    verify(userRepository).findByEmail("john@example.com");
    verify(passwordEncoder, atLeastOnce()).matches(anyString(), anyString());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar alterar para email já existente")
  void shouldThrowExceptionWhenChangingToExistingEmail() {
    UpdateUserRequest request = new UpdateUserRequest("John Doe", "existing@example.com", null, null);

    UserDetails userDetails = org.springframework.security.core.userdetails.User
        .withUsername("john@example.com")
        .password("password")
        .authorities("ROLE_USER")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.updateCurrentUser(request))
        .isInstanceOf(ConflictException.class)
        .hasMessage("Email já está em uso");

    verify(userRepository).findByEmail("john@example.com");
    verify(userRepository).existsByEmail("existing@example.com");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Deve deletar usuário atual com sucesso")
  void shouldDeleteCurrentUser() {
    UserDetails userDetails = org.springframework.security.core.userdetails.User
        .withUsername("john@example.com")
        .password("password")
        .authorities("ROLE_USER")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", user.getPasswordHash())).thenReturn(true);

    userService.deleteCurrentUser("password123");

    verify(userRepository).findByEmail("john@example.com");
    verify(passwordEncoder).matches("password123", user.getPasswordHash());
    verify(userRepository).delete(user);
  }

  @Test
  @DisplayName("Deve lançar exceção ao deletar usuário com senha inválida")
  void shouldThrowExceptionWhenDeletingUserWithInvalidPassword() {
    UserDetails userDetails = org.springframework.security.core.userdetails.User
        .withUsername("john@example.com")
        .password("password")
        .authorities("ROLE_USER")
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongPassword", user.getPasswordHash())).thenReturn(false);

    assertThatThrownBy(() -> userService.deleteCurrentUser("wrongPassword"))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Senha inválida");

    verify(userRepository).findByEmail("john@example.com");
    verify(passwordEncoder).matches("wrongPassword", user.getPasswordHash());
    verify(userRepository, never()).delete(any(User.class));
  }
}
