package com.dev.contacts.service;

import com.dev.contacts.dto.auth.AuthResponse;
import com.dev.contacts.dto.auth.LoginRequest;
import com.dev.contacts.dto.auth.SignupRequest;
import com.dev.contacts.entity.RefreshToken;
import com.dev.contacts.entity.User;
import com.dev.contacts.exception.ConflictException;
import com.dev.contacts.exception.InvalidCredentialsException;
import com.dev.contacts.exception.ResourceNotFoundException;
import com.dev.contacts.repository.UserRepository;
import com.dev.contacts.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelas operações de autenticação e autorização.
 * 
 * Gerencia registro de usuários, login, logout, refresh de tokens e
 * validações relacionadas à segurança da aplicação.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;

  /**
   * Registra um novo usuário no sistema.
   * 
   * @param request dados do usuário a ser cadastrado
   * @throws ConflictException se o email já estiver em uso
   */
  @Transactional
  public void signup(SignupRequest request) {
    log.debug("Attempting to register new user with email: {}", request.email());

    if (userRepository.existsByEmail(request.email())) {
      log.warn("Registration failed: email already in use - {}", request.email());
      throw new ConflictException("Email já está em uso");
    }

    User user = User.builder()
        .fullName(request.fullName())
        .email(request.email())
        .passwordHash(passwordEncoder.encode(request.password()))
        .build();

    userRepository.save(user);

    log.info("User registered successfully: {}", request.email());
  }

  /**
   * Realiza o login do usuário e retorna os tokens de acesso.
   * 
   * @param request credenciais de login
   * @return tokens de acesso e refresh
   * @throws InvalidCredentialsException se as credenciais forem inválidas
   */
  @Transactional
  public AuthResponse login(LoginRequest request) {
    log.debug("Login attempt for user: {}", request.email());

    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password()));

      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (BadCredentialsException e) {
      log.warn("Login failed: invalid credentials for user {}", request.email());
      throw new InvalidCredentialsException("Email ou senha inválidos");
    } catch (AuthenticationException e) {
      log.error("Authentication error for user {}: {}", request.email(), e.getMessage());
      throw new InvalidCredentialsException("Erro ao autenticar. Tente novamente.");
    }

    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> {
          log.error("User not found after successful authentication: {}", request.email());
          return new InvalidCredentialsException("Usuário não encontrado após autenticação");
        });

    String accessToken = jwtUtil.generateToken(user.getEmail());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

    log.info("User logged in successfully: {}", user.getEmail());

    return new AuthResponse(
        accessToken,
        refreshToken.getToken(),
        jwtUtil.getExpirationMs() / 1000);
  }

  /**
   * Gera um novo token de acesso usando um refresh token válido.
   * 
   * @param refreshTokenStr token de refresh
   * @return novo par de tokens
   */
  @Transactional
  public AuthResponse refreshToken(String refreshTokenStr) {
    log.debug("Attempting to refresh access token");

    RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenStr);
    User user = refreshToken.getUser();

    String newAccessToken = jwtUtil.generateToken(user.getEmail());

    log.info("Access token refreshed for user: {}", user.getEmail());

    return new AuthResponse(
        newAccessToken,
        refreshTokenStr,
        jwtUtil.getExpirationMs() / 1000);
  }

  /**
   * Realiza o logout do usuário revogando o refresh token.
   * 
   * @param refreshToken token a ser revogado
   */
  @Transactional
  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      log.debug("Logout called with empty refresh token");
      return;
    }

    try {
      refreshTokenService.revokeRefreshToken(refreshToken);
      SecurityContextHolder.clearContext();
      log.info("User logged out successfully");
    } catch (Exception e) {
      log.error("Error during logout: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    }
  }

  /**
   * Deleta a conta de um usuário após validar a senha.
   * 
   * @param userId      ID do usuário
   * @param rawPassword senha em texto plano para validação
   * @throws ResourceNotFoundException   se o usuário não for encontrado
   * @throws InvalidCredentialsException se a senha for inválida
   * @deprecated Use UserService.deleteCurrentUser() para melhor segurança
   */
  @Deprecated(since = "0.1.02", forRemoval = true)
  @Transactional
  public void deleteAccount(Long userId, String rawPassword) {
    log.debug("Account deletion requested for user ID: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

    if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
      log.warn("Account deletion failed: invalid password for user ID {}", userId);
      throw new InvalidCredentialsException("Senha inválida");
    }

    userRepository.delete(user);
    SecurityContextHolder.clearContext();

    log.info("User account deleted: ID {}", userId);
  }
}
