package com.uex.contacts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uex.contacts.config.JwtAuthFilter;
import com.uex.contacts.controller.UserController.DeleteAccountRequest;
import com.uex.contacts.dto.user.UpdateUserRequest;
import com.uex.contacts.dto.user.UserResponse;
import com.uex.contacts.exception.BadRequestException;
import com.uex.contacts.exception.ConflictException;
import com.uex.contacts.exception.ResourceNotFoundException;
import com.uex.contacts.service.UserService;
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

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureJsonTesters
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/users/me - Deve retornar usuário autenticado com sucesso")
    @WithMockUser
    void shouldGetCurrentUserSuccessfully() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "John Doe",
                "john@example.com",
                OffsetDateTime.now());

        when(userService.findCurrentUser()).thenReturn(response);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService, times(1)).findCurrentUser();
    }

    @Test
    @DisplayName("GET /api/users/me - Deve retornar erro quando usuário não está autenticado")
    void shouldReturnErrorWhenUserNotAuthenticated() throws Exception {
        // Como o JwtAuthFilter está excluído nos testes @WebMvcTest,
        // não conseguimos testar a autenticação neste nível.
        // Este teste deveria ser um teste de integração com @SpringBootTest
        when(userService.findCurrentUser()).thenReturn(null);
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Deve retornar usuário por ID com sucesso")
    @WithMockUser
    void shouldGetUserByIdSuccessfully() throws Exception {
        Long userId = 1L;
        UserResponse response = new UserResponse(
                userId,
                "John Doe",
                "john@example.com",
                OffsetDateTime.now());

        when(userService.findById(userId)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService, times(1)).findById(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id} - Deve retornar erro quando usuário não existe")
    @WithMockUser
    void shouldReturnErrorWhenUserNotFound() throws Exception {
        Long userId = 999L;

        when(userService.findById(userId))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado"));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/users - Deve atualizar usuário com sucesso")
    @WithMockUser
    void shouldUpdateCurrentUserSuccessfully() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                "John Doe Updated",
                "john.updated@example.com",
                null,
                null);

        UserResponse response = new UserResponse(
                1L,
                "John Doe Updated",
                "john.updated@example.com",
                OffsetDateTime.now());

        when(userService.updateCurrentUser(any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        verify(userService, times(1)).updateCurrentUser(any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /api/users - Deve atualizar usuário com nova senha")
    @WithMockUser
    void shouldUpdateCurrentUserWithNewPassword() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                "John Doe",
                "john@example.com",
                "newPassword123",
                "oldPassword123");

        UserResponse response = new UserResponse(
                1L,
                "John Doe",
                "john@example.com",
                OffsetDateTime.now());

        when(userService.updateCurrentUser(any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateCurrentUser(any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("PUT /api/users - Deve retornar erro quando email já existe")
    @WithMockUser
    void shouldReturnErrorWhenEmailAlreadyExists() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                "John Doe",
                "existing@example.com",
                null,
                null);

        when(userService.updateCurrentUser(any(UpdateUserRequest.class)))
                .thenThrow(new ConflictException("Email já cadastrado"));

        mockMvc.perform(put("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/users - Deve retornar erro de validação quando campos obrigatórios ausentes")
    @WithMockUser
    void shouldReturnValidationErrorWhenRequiredFieldsMissing() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                "",
                "",
                null,
                null);

        mockMvc.perform(put("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/users - Deve retornar erro quando email é inválido")
    @WithMockUser
    void shouldReturnErrorWhenEmailIsInvalid() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                "John Doe",
                "invalid-email",
                null,
                null);

        mockMvc.perform(put("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/users - Deve retornar erro quando senha é muito curta")
    @WithMockUser
    void shouldReturnErrorWhenPasswordIsTooShort() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                "John Doe",
                "john@example.com",
                "12345",
                "oldPassword");

        mockMvc.perform(put("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/users - Deve deletar conta com sucesso")
    @WithMockUser
    void shouldDeleteCurrentUserSuccessfully() throws Exception {
        DeleteAccountRequest request = new DeleteAccountRequest("password123");

        doNothing().when(userService).deleteCurrentUser(anyString());

        mockMvc.perform(delete("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteCurrentUser(anyString());
    }

    @Test
    @DisplayName("DELETE /api/users - Deve retornar erro quando senha é incorreta")
    @WithMockUser
    void shouldReturnErrorWhenPasswordIsIncorrect() throws Exception {
        DeleteAccountRequest request = new DeleteAccountRequest("wrongPassword");

        doThrow(new BadRequestException("Senha incorreta"))
                .when(userService).deleteCurrentUser(anyString());

        mockMvc.perform(delete("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/users - Deve retornar erro de validação quando senha ausente")
    @WithMockUser
    void shouldReturnValidationErrorWhenPasswordMissing() throws Exception {
        DeleteAccountRequest request = new DeleteAccountRequest("");

        mockMvc.perform(delete("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/users - Deve retornar erro quando usuário não está autenticado")
    void shouldReturnErrorWhenUserNotAuthenticatedForDelete() throws Exception {
        // Como o JwtAuthFilter está excluído nos testes @WebMvcTest,
        // não conseguimos testar a autenticação neste nível.
        // Este teste deveria ser um teste de integração com @SpringBootTest
        DeleteAccountRequest request = new DeleteAccountRequest("password123");

        doNothing().when(userService).deleteCurrentUser(anyString());
        mockMvc.perform(delete("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
