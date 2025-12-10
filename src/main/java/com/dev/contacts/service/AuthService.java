package com.dev.contacts.service;

import com.dev.contacts.dto.auth.AuthResponse;
import com.dev.contacts.dto.auth.LoginRequest;
import com.dev.contacts.dto.auth.SignupRequest;
import com.dev.contacts.entity.RefreshToken;
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
  private final RefreshTokenService refreshTokenService;

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

    String accessToken = jwtUtil.generateToken(user.getEmail());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

    return new AuthResponse(
        accessToken,
        refreshToken.getToken(),
        jwtUtil.getExpirationMs() / 1000);
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

  public AuthResponse refreshToken(String refreshTokenStr) {
    RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenStr);
    User user = refreshToken.getUser();

    String newAccessToken = jwtUtil.generateToken(user.getEmail());

    return new AuthResponse(
        newAccessToken,
        refreshTokenStr,
        jwtUtil.getExpirationMs() / 1000);
  }

  public void logout(String refreshToken) {
    if (refreshToken != null && !refreshToken.isBlank()) {
      refreshTokenService.revokeRefreshToken(refreshToken);
    }
  }
}
