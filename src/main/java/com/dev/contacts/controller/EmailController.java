package com.dev.contacts.controller;

import com.dev.contacts.dto.email.EmailRequest;
import com.dev.contacts.dto.email.EmailResponse;
import com.dev.contacts.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Tag(name = "Email", description = "Email sending endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EmailController {
  private final EmailService emailService;

  @PostMapping("/send")
  @Operation(summary = "Send email", description = "Sends an email with a hardcoded message")
  public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
    EmailResponse response = emailService.sendEmail(request);

    if (response.isSuccess()) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(500).body(response);
    }
  }
}
