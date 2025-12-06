package com.uex.contacts.service;

import com.uex.contacts.dto.auth.AuthResponse;
import com.uex.contacts.dto.auth.LoginRequest;
import com.uex.contacts.dto.auth.SignupRequest;
import com.uex.contacts.entity.User;
import com.uex.contacts.exception.ConflictException;
import com.uex.contacts.exception.InvalidCredentialsException;
import com.uex.contacts.repository.UserRepository;
import com.uex.contacts.config.JwtUtil;
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
