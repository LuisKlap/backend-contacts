package com.dev.contacts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dev.contacts.config.JwtAuthFilter;
import com.dev.contacts.dto.contact.ContactRequest;
import com.dev.contacts.dto.contact.ContactResponse;
import com.dev.contacts.entity.User;
import com.dev.contacts.exception.ResourceNotFoundException;
import com.dev.contacts.service.ContactService;
import com.dev.contacts.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import java.util.Optional;

@WebMvcTest(controllers = ContactController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureJsonTesters
@Import(com.dev.contacts.config.MockMvcTestConfig.class)
@DisplayName("ContactController Tests")
class ContactControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private ContactService contactService;

  @MockitoBean
  private UserService userService;

  private User createMockUser() {
    return User.builder()
        .id(1L)
        .email("user@example.com")
        .fullName("Test User")
        .passwordHash("hashedpassword")
        .build();
  }

  private Authentication createAuthentication(User user) {
    // Cria autenticação com User diretamente como principal (ele implementa
    // UserDetails)
    // O terceiro parâmetro (authorities) faz o token ser "authenticated"
    return new UsernamePasswordAuthenticationToken(
        user, // principal será o User
        user.getPassword(), // credentials
        user.getAuthorities() // authorities - isso marca como authenticated
    );
  }

  // Helper method que configura o SecurityContext para o teste
  private org.springframework.test.web.servlet.request.RequestPostProcessor authenticateUser(User user) {
    // Mocka o userService para retornar o usuário quando getAuthenticatedUser()
    // buscar no banco
    // Isso é necessário porque User implementa UserDetails, então o controller vai
    // extrair o username
    // e buscar no userService
    when(userService.findEntityByEmail(user.getEmail())).thenReturn(Optional.of(user));
    return SecurityMockMvcRequestPostProcessors.authentication(createAuthentication(user));
  }

  @Test
  @DisplayName("POST /api/contacts - Deve criar contato com sucesso")
  void shouldCreateContactSuccessfully() throws Exception {
    ContactRequest request = new ContactRequest(
        "João Silva",
        "12345678901",
        "(11) 98765-4321",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "Bela Vista",
        "1000",
        "Apto 101");

    ContactResponse response = new ContactResponse(
        1L,
        "João Silva",
        "12345678901",
        "(11) 98765-4321",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "1000",
        "Apto 101",
        "Bela Vista",
        -23.5505199,
        -46.6333094,
        OffsetDateTime.now(),
        OffsetDateTime.now());

    User mockUser = createMockUser();

    when(contactService.createContact(any(User.class), any(ContactRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/contacts")
        .with(csrf())
        .with(authenticateUser(mockUser))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("João Silva"))
        .andExpect(jsonPath("$.cpf").value("12345678901"))
        .andExpect(jsonPath("$.phone").value("(11) 98765-4321"))
        .andExpect(jsonPath("$.cep").value("01310-100"))
        .andExpect(jsonPath("$.state").value("SP"))
        .andExpect(jsonPath("$.city").value("São Paulo"))
        .andExpect(jsonPath("$.street").value("Avenida Paulista"))
        .andExpect(jsonPath("$.number").value("1000"))
        .andExpect(jsonPath("$.complement").value("Apto 101"))
        .andExpect(jsonPath("$.neighborhood").value("Bela Vista"));

    verify(contactService, times(1)).createContact(any(User.class), any(ContactRequest.class));
  }

  @Test
  @DisplayName("POST /api/contacts - Deve retornar erro 400 quando dados inválidos")
  void shouldReturnBadRequestWhenInvalidData() throws Exception {
    ContactRequest invalidRequest = new ContactRequest(
        "", // nome vazio
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        null);

    mockMvc.perform(post("/api/contacts")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verify(contactService, never()).createContact(any(), any());
  }

  @Test
  @DisplayName("POST /api/contacts - Deve retornar erro quando usuário não encontrado")
  void shouldReturnErrorWhenUserNotFound() throws Exception {
    ContactRequest request = new ContactRequest(
        "João Silva",
        "12345678901",
        "(11) 98765-4321",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "Bela Vista",
        "1000",
        null);

    // Este teste não precisa de autenticação pois a validação de dados acontece
    // antes
    mockMvc.perform(post("/api/contacts")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(contactService, never()).createContact(any(), any());
  }

  @Test
  @DisplayName("PUT /api/contacts/{id} - Deve atualizar contato com sucesso")
  void shouldUpdateContactSuccessfully() throws Exception {
    Long contactId = 1L;
    ContactRequest request = new ContactRequest(
        "João Silva Atualizado",
        "12345678901",
        "(11) 98765-4321",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "Bela Vista",
        "2000",
        "Apto 202");

    ContactResponse response = new ContactResponse(
        contactId,
        "João Silva Atualizado",
        "12345678901",
        "(11) 98765-4321",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "2000",
        "Apto 202",
        "Bela Vista",
        -23.5505199,
        -46.6333094,
        OffsetDateTime.now(),
        OffsetDateTime.now());

    User mockUser = createMockUser();

    when(contactService.updateContact(eq(contactId), any(User.class), any(ContactRequest.class)))
        .thenReturn(response);

    mockMvc.perform(put("/api/contacts/{id}", contactId)
        .with(csrf())
        .with(authenticateUser(mockUser))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(contactId))
        .andExpect(jsonPath("$.name").value("João Silva Atualizado"))
        .andExpect(jsonPath("$.number").value("2000"))
        .andExpect(jsonPath("$.complement").value("Apto 202"));

    verify(contactService, times(1)).updateContact(eq(contactId), any(User.class), any(ContactRequest.class));
  }

  @Test
  @DisplayName("PUT /api/contacts/{id} - Deve retornar erro 404 quando contato não encontrado")
  void shouldReturnNotFoundWhenUpdatingNonExistentContact() throws Exception {
    Long contactId = 999L;
    ContactRequest request = new ContactRequest(
        "João Silva",
        "12345678901",
        "(11) 98765-4321",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "Bela Vista",
        "1000",
        null);

    User mockUser = createMockUser();

    when(contactService.updateContact(eq(contactId), any(User.class), any(ContactRequest.class)))
        .thenThrow(new ResourceNotFoundException("Contato não encontrado"));

    mockMvc.perform(put("/api/contacts/{id}", contactId)
        .with(csrf())
        .with(authenticateUser(mockUser))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());

    verify(contactService, times(1)).updateContact(eq(contactId), any(User.class), any(ContactRequest.class));
  }

  @Test
  @DisplayName("GET /api/contacts - Deve listar contatos com sucesso")
  void shouldListContactsSuccessfully() throws Exception {
    ContactResponse contact1 = new ContactResponse(
        1L,
        "João Silva",
        "12345678901",
        "(11) 98765-4321",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "1000",
        null,
        "Bela Vista",
        -23.5505199,
        -46.6333094,
        OffsetDateTime.now(),
        OffsetDateTime.now());

    ContactResponse contact2 = new ContactResponse(
        2L,
        "Maria Santos",
        "98765432100",
        "(11) 91234-5678",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "1500",
        "Sala 10",
        "Bela Vista",
        -23.5505199,
        -46.6333094,
        OffsetDateTime.now(),
        OffsetDateTime.now());

    Page<ContactResponse> page = new PageImpl<>(List.of(contact1, contact2), PageRequest.of(0, 10), 2);

    User mockUser = createMockUser();

    when(contactService.listContacts(any(User.class), isNull(), any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/api/contacts")
        .param("page", "0")
        .param("size", "10")
        .with(authenticateUser(mockUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].name").value("João Silva"))
        .andExpect(jsonPath("$.content[1].name").value("Maria Santos"))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.totalPages").value(1));

    verify(contactService, times(1)).listContacts(any(User.class), isNull(), any(Pageable.class));
  }

  @Test
  @DisplayName("GET /api/contacts - Deve listar contatos com filtro")
  void shouldListContactsWithFilter() throws Exception {
    ContactResponse contact = new ContactResponse(
        1L,
        "João Silva",
        "12345678901",
        "(11) 98765-4321",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "1000",
        null,
        "Bela Vista",
        -23.5505199,
        -46.6333094,
        OffsetDateTime.now(),
        OffsetDateTime.now());

    Page<ContactResponse> page = new PageImpl<>(List.of(contact), PageRequest.of(0, 10), 1);

    User mockUser = createMockUser();

    when(contactService.listContacts(any(User.class), eq("João"), any(Pageable.class))).thenReturn(page);

    mockMvc.perform(get("/api/contacts")
        .with(authenticateUser(mockUser))
        .param("filter", "João")
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("João Silva"))
        .andExpect(jsonPath("$.totalElements").value(1));

    verify(contactService, times(1)).listContacts(any(User.class), eq("João"), any(Pageable.class));
  }

  @Test
  @DisplayName("GET /api/contacts/{id} - Deve buscar contato por ID com sucesso")
  void shouldGetContactByIdSuccessfully() throws Exception {
    Long contactId = 1L;
    ContactResponse response = new ContactResponse(
        contactId,
        "João Silva",
        "12345678901",
        "(11) 98765-4321",
        "01310-100",
        "SP",
        "São Paulo",
        "Avenida Paulista",
        "1000",
        "Apto 101",
        "Bela Vista",
        -23.5505199,
        -46.6333094,
        OffsetDateTime.now(),
        OffsetDateTime.now());

    User mockUser = createMockUser();

    when(contactService.getContact(eq(contactId), any(User.class))).thenReturn(response);

    mockMvc.perform(get("/api/contacts/{id}", contactId)
        .with(authenticateUser(mockUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(contactId))
        .andExpect(jsonPath("$.name").value("João Silva"))
        .andExpect(jsonPath("$.cpf").value("12345678901"));

    verify(contactService, times(1)).getContact(eq(contactId), any(User.class));
  }

  @Test
  @DisplayName("GET /api/contacts/{id} - Deve retornar erro 404 quando contato não encontrado")
  void shouldReturnNotFoundWhenContactDoesNotExist() throws Exception {
    Long contactId = 999L;

    User mockUser = createMockUser();

    when(contactService.getContact(eq(contactId), any(User.class)))
        .thenThrow(new ResourceNotFoundException("Contato não encontrado"));

    mockMvc.perform(get("/api/contacts/{id}", contactId)
        .with(authenticateUser(mockUser)))
        .andExpect(status().isNotFound());

    verify(contactService, times(1)).getContact(eq(contactId), any(User.class));
  }

  @Test
  @DisplayName("DELETE /api/contacts/{id} - Deve deletar contato com sucesso")
  void shouldDeleteContactSuccessfully() throws Exception {
    Long contactId = 1L;

    User mockUser = createMockUser();

    doNothing().when(contactService).deleteContact(eq(contactId), any(User.class));

    mockMvc.perform(delete("/api/contacts/{id}", contactId)
        .with(csrf())
        .with(authenticateUser(mockUser)))
        .andExpect(status().isOk());

    verify(contactService, times(1)).deleteContact(eq(contactId), any(User.class));
  }

  @Test
  @DisplayName("DELETE /api/contacts/{id} - Deve retornar erro 404 quando contato não encontrado")
  void shouldReturnNotFoundWhenDeletingNonExistentContact() throws Exception {
    Long contactId = 999L;

    User mockUser = createMockUser();

    doThrow(new ResourceNotFoundException("Contato não encontrado"))
        .when(contactService).deleteContact(eq(contactId), any(User.class));

    mockMvc.perform(delete("/api/contacts/{id}", contactId)
        .with(csrf())
        .with(authenticateUser(mockUser)))
        .andExpect(status().isNotFound());

    verify(contactService, times(1)).deleteContact(eq(contactId), any(User.class));
  }

  @Test
  @DisplayName("GET /api/contacts - Deve retornar lista vazia quando não há contatos")
  void shouldReturnEmptyListWhenNoContacts() throws Exception {
    Page<ContactResponse> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

    User mockUser = createMockUser();

    when(contactService.listContacts(any(User.class), isNull(), any(Pageable.class))).thenReturn(emptyPage);

    mockMvc.perform(get("/api/contacts")
        .with(authenticateUser(mockUser))
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.totalElements").value(0));

    verify(contactService, times(1)).listContacts(any(User.class), isNull(), any(Pageable.class));
  }
}
