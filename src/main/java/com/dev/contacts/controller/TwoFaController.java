package com.dev.contacts.controller;

import com.dev.contacts.entity.User;
import com.dev.contacts.repository.UserRepository;
import com.dev.contacts.service.TotpService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TwoFaController {
  private final TotpService totpService;
  private final UserRepository userRepository;

  @PostMapping(value = "/setup", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<byte[]> setup2fa(@AuthenticationPrincipal UserDetails principal) {
    User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
    var key = totpService.createCredentials();
    user.setTwoFactorSecret(key.getKey());
    user.setTwoFactorType("authenticator");
    userRepository.save(user);
    String issuer = "ContactsApp";
    String label = user.getEmail();
    String secret = key.getKey();
    String otpUrl = String.format(
        "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
        URLEncoder.encode(issuer, StandardCharsets.UTF_8),
        URLEncoder.encode(label, StandardCharsets.UTF_8),
        secret,
        URLEncoder.encode(issuer, StandardCharsets.UTF_8));
    try {
      byte[] qrPng = totpService.generateQrPng(otpUrl, 240, 240);
      return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrPng);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  public static class Confirm2faRequest {
    @NotBlank
    @JsonProperty("code")
    private String code;

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }
  }

  @PostMapping("/confirm")
  public ResponseEntity<?> confirm(@RequestBody Confirm2faRequest request,
      @AuthenticationPrincipal UserDetails principal) {
    User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
    int code;
    try {
      code = Integer.parseInt(request.getCode());
    } catch (NumberFormatException e) {
      return ResponseEntity.badRequest().body("Código inválido");
    }
    boolean ok = totpService.verifyCode(user.getTwoFactorSecret(), code);
    if (ok) {
      user.setTwoFactorEnabled(true);
      userRepository.save(user);
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
