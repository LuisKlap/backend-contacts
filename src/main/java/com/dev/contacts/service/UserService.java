package com.dev.contacts.service;

import com.dev.contacts.dto.user.UpdateUserRequest;
import com.dev.contacts.dto.user.UserResponse;
import com.dev.contacts.entity.User;
import com.dev.contacts.exception.ConflictException;
import com.dev.contacts.exception.InvalidCredentialsException;
import com.dev.contacts.exception.ResourceNotFoundException;
import com.dev.contacts.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(usernameOrEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + usernameOrEmail));

    return org.springframework.security.core.userdetails.User
        .withUsername(user.getUsername())
        .password(user.getPassword())
        .authorities(user.getAuthorities())
        .accountLocked(false)
        .disabled(false)
        .build();
  }

  public UserResponse findById(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    return toUserResponse(user);
  }

  public Optional<User> findEntityByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public UserResponse findCurrentUser() {
    String email = extractAuthenticatedEmail();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

    return toUserResponse(user);
  }

  @Transactional
  public UserResponse updateCurrentUser(UpdateUserRequest request) {
    String email = extractAuthenticatedEmail();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

    // Se está tentando alterar a senha, valida a senha atual
    if (request.password() != null && !request.password().isBlank()) {
      if (request.currentPassword() == null || request.currentPassword().isBlank()) {
        throw new InvalidCredentialsException("Senha atual é obrigatória para alterar a senha");
      }
      if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
        throw new InvalidCredentialsException("Senha atual inválida");
      }
      user.setPasswordHash(passwordEncoder.encode(request.password()));
    }

    // Verifica se o email já está em uso por outro usuário
    if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
      throw new ConflictException("Email já está em uso");
    }

    // Atualiza os dados
    user.setFullName(request.fullName());
    user.setEmail(request.email());

    User updatedUser = userRepository.save(user);
    return toUserResponse(updatedUser);
  }

  @Transactional
  public void deleteCurrentUser(String rawPassword) {
    String email = extractAuthenticatedEmail();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

    if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
      throw new InvalidCredentialsException("Senha inválida");
    }

    userRepository.delete(user);
    SecurityContextHolder.clearContext();
  }

  private UserResponse toUserResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getCreatedAt());
  }

  private String extractAuthenticatedEmail() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
      return ud.getUsername();
    } else if (principal instanceof String s) {
      return s;
    }

    throw new ResourceNotFoundException("Usuário autenticado não identificado");
  }
}
