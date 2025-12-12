package com.dev.contacts.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
  @NotBlank(message = "Recipient email is required")
  @Email(message = "Invalid email format")
  private String to;

  @NotBlank(message = "Subject is required")
  private String subject;
}
