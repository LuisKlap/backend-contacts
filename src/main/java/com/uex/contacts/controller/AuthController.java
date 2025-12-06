package com.uex.contacts.controller;

import com.uex.contacts.dto.auth.AuthResponse;
import com.uex.contacts.dto.auth.LoginRequest;
import com.uex.contacts.dto.auth.SignupRequest;
import com.uex.contacts.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signup")
  public void signup(@Valid @RequestBody SignupRequest request) {
    authService.signup(request);
    return;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }
}
