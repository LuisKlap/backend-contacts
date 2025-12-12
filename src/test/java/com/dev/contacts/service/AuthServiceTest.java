package com.dev.contacts.service;

import com.dev.contacts.config.JwtUtil;
import com.dev.contacts.dto.auth.AuthResponse;
import com.dev.contacts.dto.auth.LoginRequest;
import com.dev.contacts.dto.auth.SignupRequest;
import com.dev.contacts.entity.RefreshToken;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private AuthService authService;

  private SignupRequest signupRequest;
  private LoginRequest loginRequest;
  private User user;

  @BeforeEach
  void setUp() {
    signupRequest = new SignupRequest("John Doe", "john@example.com", "password123");
    loginRequest = new LoginRequest("john@example.com", "password123");

    user = User.builder()
        .id(1L)
        .fullName("John Doe")
        .email("john@example.com")
        .passwordHash("hashedPassword")
        .build();
  }

  @Test
  @DisplayName("Deve criar uma nova conta com sucesso")
  void shouldSignupSuccessfully() {
    when(userRepository.existsByEmail(signupRequest.email())).thenReturn(false);
    when(passwordEncoder.encode(signupRequest.password())).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    authService.signup(signupRequest);

    verify(userRepository).existsByEmail(signupRequest.email());
    verify(passwordEncoder).encode(signupRequest.password());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar criar conta com email já existente")
  void shouldThrowExceptionWhenEmailAlreadyExists() {
    when(userRepository.existsByEmail(signupRequest.email())).thenReturn(true);

    assertThatThrownBy(() -> authService.signup(signupRequest))
        .isInstanceOf(ConflictException.class)
        .hasMessage("Email já está em uso");

    verify(userRepository).existsByEmail(signupRequest.email());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Deve fazer login com sucesso")
  void shouldLoginSuccessfully() {
    Authentication authentication = mock(Authentication.class);
    RefreshToken refreshToken = RefreshToken.builder()
        .token("refresh-token")
        .user(user)
        .build();

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken(user.getEmail())).thenReturn("jwt-access-token");
    when(jwtUtil.getExpirationMs()).thenReturn(3600000L);
    when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

    AuthResponse response = authService.login(loginRequest);

    assertThat(response).isNotNull();
    assertThat(response.accessToken()).isEqualTo("jwt-access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.expiresIn()).isEqualTo(3600L);

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository).findByEmail(loginRequest.email());
    verify(jwtUtil).generateToken(user.getEmail());
    verify(refreshTokenService).createRefreshToken(user);
  }

  @Test
  @DisplayName("Deve lançar exceção ao fazer login com credenciais inválidas")
  void shouldThrowExceptionWhenLoginWithInvalidCredentials() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Email ou senha inválidos");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository, never()).findByEmail(anyString());
  }

  @Test
  @DisplayName("Deve lançar exceção quando usuário não é encontrado após autenticação")
  void shouldThrowExceptionWhenUserNotFoundAfterAuthentication() {
    Authentication authentication = mock(Authentication.class);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(loginRequest))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Usuário não encontrado após autenticação");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository).findByEmail(loginRequest.email());
  }

  @Test
  @DisplayName("Deve deletar conta com senha válida")
  @SuppressWarnings("deprecation")
  void shouldDeleteAccountWithValidPassword() {
    String rawPassword = "password123";

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(rawPassword, user.getPasswordHash())).thenReturn(true);

    authService.deleteAccount(user.getId(), rawPassword);

    verify(userRepository).findById(user.getId());
    verify(passwordEncoder).matches(rawPassword, user.getPasswordHash());
    verify(userRepository).delete(user);
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar deletar conta com usuário não encontrado")
  @SuppressWarnings("deprecation")
  void shouldThrowExceptionWhenDeletingAccountWithUserNotFound() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.deleteAccount(999L, "password123"))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(userRepository).findById(999L);
    verify(userRepository, never()).delete(any(User.class));
  }

  @Test
  @DisplayName("Deve lançar exceção ao tentar deletar conta com senha inválida")
  @SuppressWarnings("deprecation")
  void shouldThrowExceptionWhenDeletingAccountWithInvalidPassword() {
    String rawPassword = "wrongPassword";

    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(rawPassword, user.getPasswordHash())).thenReturn(false);

    assertThatThrownBy(() -> authService.deleteAccount(user.getId(), rawPassword))
        .isInstanceOf(InvalidCredentialsException.class);

    verify(userRepository).findById(user.getId());
    verify(passwordEncoder).matches(rawPassword, user.getPassword());
    verify(userRepository, never()).delete(any(User.class));
  }
}
