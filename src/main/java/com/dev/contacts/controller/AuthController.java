package com.dev.contacts.controller;

import com.dev.contacts.dto.auth.AuthResponse;
import com.dev.contacts.dto.auth.ForgotPasswordRequest;
import com.dev.contacts.dto.auth.ForgotPasswordResponse;
import com.dev.contacts.dto.auth.LoginRequest;
import com.dev.contacts.dto.auth.LogoutRequest;
import com.dev.contacts.dto.auth.RefreshTokenRequest;
import com.dev.contacts.dto.auth.ResetPasswordRequest;
import com.dev.contacts.dto.auth.ResetPasswordResponse;
import com.dev.contacts.dto.auth.SignupRequest;
import com.dev.contacts.service.AuthService;
import com.dev.contacts.service.PasswordResetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

  private final AuthService authService;
  private final PasswordResetService passwordResetService;

  @PostMapping("/signup")
  @Operation(summary = "Register a new user")
  public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
    authService.signup(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  @Operation(summary = "Login and receive access and refresh tokens")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Refresh access token using refresh token")
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    AuthResponse response = authService.refreshToken(request.refreshToken());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout and revoke refresh token")
  public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
    authService.logout(request.refreshToken());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/forgot-password")
  @Operation(summary = "Request password reset")
  public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    ForgotPasswordResponse response = passwordResetService.requestPasswordReset(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Reset password using token")
  public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    ResetPasswordResponse response = passwordResetService.resetPassword(request);
    return ResponseEntity.ok(response);
  }
}
