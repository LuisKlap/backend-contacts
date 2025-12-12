package com.dev.contacts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dev.contacts.config.JwtAuthFilter;
import com.dev.contacts.dto.auth.AuthResponse;
import com.dev.contacts.dto.auth.LoginRequest;
import com.dev.contacts.dto.auth.SignupRequest;
import com.dev.contacts.exception.BadRequestException;
import com.dev.contacts.exception.ConflictException;
import com.dev.contacts.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureJsonTesters
@DisplayName("AuthController Tests")
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @MockitoBean
        private AuthService authService;

        @MockitoBean
        private com.dev.contacts.service.PasswordResetService passwordResetService;

        @Test
        @DisplayName("POST /api/auth/signup - Deve criar usuário com sucesso")
        void shouldSignupSuccessfully() throws Exception {
                SignupRequest request = new SignupRequest(
                                "John Doe",
                                "john@example.com",
                                "password123");

                doNothing().when(authService).signup(any(SignupRequest.class));

                mockMvc.perform(post("/api/auth/signup")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());

                verify(authService, times(1)).signup(any(SignupRequest.class));
        }

        @Test
        @DisplayName("POST /api/auth/signup - Deve retornar erro quando email já existe")
        void shouldReturnErrorWhenEmailAlreadyExists() throws Exception {
                SignupRequest request = new SignupRequest(
                                "John Doe",
                                "john@example.com",
                                "password123");

                doThrow(new ConflictException("Email já cadastrado"))
                                .when(authService).signup(any(SignupRequest.class));

                mockMvc.perform(post("/api/auth/signup")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("POST /api/auth/signup - Deve retornar erro de validação quando campos obrigatórios ausentes")
        void shouldReturnValidationErrorWhenRequiredFieldsMissing() throws Exception {
                SignupRequest request = new SignupRequest(
                                "",
                                "",
                                "");

                mockMvc.perform(post("/api/auth/signup")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/signup - Deve retornar erro quando email é inválido")
        void shouldReturnErrorWhenEmailIsInvalid() throws Exception {
                SignupRequest request = new SignupRequest(
                                "John Doe",
                                "invalid-email",
                                "password123");

                mockMvc.perform(post("/api/auth/signup")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/signup - Deve retornar erro quando senha é muito curta")
        void shouldReturnErrorWhenPasswordIsTooShort() throws Exception {
                SignupRequest request = new SignupRequest(
                                "John Doe",
                                "john@example.com",
                                "12345");

                mockMvc.perform(post("/api/auth/signup")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/login - Deve fazer login com sucesso")
        void shouldLoginSuccessfully() throws Exception {
                LoginRequest request = new LoginRequest(
                                "john@example.com",
                                "password123");

                AuthResponse response = new AuthResponse(
                                "jwt-access-token-here",
                                "jwt-refresh-token-here",
                                3600L);

                when(authService.login(any(LoginRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("jwt-access-token-here"))
                                .andExpect(jsonPath("$.refreshToken").value("jwt-refresh-token-here"))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.expiresIn").value(3600));

                verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("POST /api/auth/login - Deve retornar erro quando credenciais inválidas")
        void shouldReturnErrorWhenCredentialsInvalid() throws Exception {
                LoginRequest request = new LoginRequest(
                                "john@example.com",
                                "wrongpassword");

                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(new BadRequestException("Email ou senha inválidos"));

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/login - Deve retornar erro de validação quando campos obrigatórios ausentes")
        void shouldReturnValidationErrorWhenLoginFieldsMissing() throws Exception {
                LoginRequest request = new LoginRequest(
                                "",
                                "");

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/login - Deve retornar erro quando email é inválido")
        void shouldReturnErrorWhenLoginEmailIsInvalid() throws Exception {
                LoginRequest request = new LoginRequest(
                                "invalid-email",
                                "password123");

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/login - Deve retornar erro quando senha é muito curta")
        void shouldReturnErrorWhenLoginPasswordIsTooShort() throws Exception {
                LoginRequest request = new LoginRequest(
                                "john@example.com",
                                "12345");

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/login - Deve retornar erro quando usuário não existe")
        void shouldReturnErrorWhenUserNotFound() throws Exception {
                LoginRequest request = new LoginRequest(
                                "nonexistent@example.com",
                                "password123");

                when(authService.login(any(LoginRequest.class)))
                                .thenThrow(new BadRequestException("Email ou senha inválidos"));

                mockMvc.perform(post("/api/auth/login")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }
}
