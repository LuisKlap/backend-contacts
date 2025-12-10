package com.dev.contacts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dev.contacts.config.JwtAuthFilter;
import com.dev.contacts.dto.email.EmailRequest;
import com.dev.contacts.dto.email.EmailResponse;
import com.dev.contacts.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmailController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureJsonTesters
@DisplayName("EmailController Tests")
class EmailControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private EmailService emailService;

  @Test
  @DisplayName("POST /api/email/send - Deve enviar email com sucesso")
  @WithMockUser(username = "user@example.com")
  void shouldSendEmailSuccessfully() throws Exception {
    EmailRequest request = EmailRequest.builder()
        .to("test@example.com")
        .subject("Test Subject")
        .build();

    EmailResponse response = EmailResponse.builder()
        .message("Email sent successfully")
        .to("test@example.com")
        .subject("Test Subject")
        .success(true)
        .build();

    when(emailService.sendEmail(any(EmailRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/email/send")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.to", is("test@example.com")))
        .andExpect(jsonPath("$.subject", is("Test Subject")))
        .andExpect(jsonPath("$.message", is("Email sent successfully")));
  }

  @Test
  @DisplayName("POST /api/email/send - Deve retornar erro 500 quando falha ao enviar email")
  @WithMockUser(username = "user@example.com")
  void shouldReturn500WhenEmailFailsToSend() throws Exception {
    EmailRequest request = EmailRequest.builder()
        .to("test@example.com")
        .subject("Test Subject")
        .build();

    EmailResponse response = EmailResponse.builder()
        .message("Failed to send email: Mail server error")
        .to("test@example.com")
        .subject("Test Subject")
        .success(false)
        .build();

    when(emailService.sendEmail(any(EmailRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/email/send")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.to", is("test@example.com")))
        .andExpect(jsonPath("$.message").value(containsString("Failed to send email")));
  }

  @Test
  @DisplayName("POST /api/email/send - Deve retornar erro 400 quando email é inválido")
  @WithMockUser(username = "user@example.com")
  void shouldReturn400WhenEmailIsInvalid() throws Exception {
    EmailRequest request = EmailRequest.builder()
        .to("invalid-email")
        .subject("Test Subject")
        .build();

    mockMvc.perform(post("/api/email/send")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /api/email/send - Deve retornar erro 400 quando subject está vazio")
  @WithMockUser(username = "user@example.com")
  void shouldReturn400WhenSubjectIsEmpty() throws Exception {
    EmailRequest request = EmailRequest.builder()
        .to("test@example.com")
        .subject("")
        .build();

    mockMvc.perform(post("/api/email/send")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
