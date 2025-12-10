package com.dev.contacts.service;

import com.dev.contacts.dto.auth.AuthResponse;
import com.dev.contacts.dto.auth.LoginRequest;
import com.dev.contacts.dto.auth.SignupRequest;
import com.dev.contacts.entity.User;
import com.dev.contacts.exception.ConflictException;
import com.dev.contacts.exception.InvalidCredentialsException;
import com.dev.contacts.repository.UserRepository;
import com.dev.contacts.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;

  public void signup(SignupRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new ConflictException("Email already in use");
    }

    User user = new User();
    user.setFullName(request.fullName());
    user.setEmail(request.email());
    user.setPasswordHash(passwordEncoder.encode(request.password()));

    userRepository.save(user);
  }

  public AuthResponse login(LoginRequest request) {
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(request.email(),
        request.password());

    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(authToken);
    } catch (Exception ex) {
      throw new InvalidCredentialsException("Invalid email or password");
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);

    User user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> new InvalidCredentialsException("User not found after authentication"));

    String token = jwtUtil.generateToken(user.getEmail());
    return new AuthResponse(token, null);
  }

  public boolean deleteAccount(Long userId, String rawPassword) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new InvalidCredentialsException("Invalid password");
    }

    userRepository.delete(user);
    return true;
  }
}
