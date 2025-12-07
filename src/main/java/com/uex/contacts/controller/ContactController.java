package com.uex.contacts.controller;

import com.uex.contacts.dto.contact.ContactRequest;
import com.uex.contacts.dto.contact.ContactResponse;
import com.uex.contacts.entity.User;
import com.uex.contacts.exception.BadRequestException;
import com.uex.contacts.service.ContactService;
import com.uex.contacts.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
  private final ContactService contactService;
  private final UserService userService;

  @PostMapping
  public ContactResponse create(
      Authentication authentication,
      @Valid @RequestBody ContactRequest request) {

    User owner = getAuthenticatedUser(authentication);
    return contactService.createContact(owner, request);
  }

  @PutMapping("/{id}")
  public ContactResponse update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody ContactRequest request) {

    User owner = getAuthenticatedUser(authentication);
    return contactService.updateContact(id, owner, request);
  }

  @GetMapping
  public Page<ContactResponse> list(
      Authentication authentication,
      @RequestParam(required = false) String filter,
      Pageable pageable) {

    User owner = getAuthenticatedUser(authentication);
    return contactService.listContacts(owner, filter, pageable);
  }

  @GetMapping("/{id}")
  public ContactResponse getOne(
      Authentication authentication,
      @PathVariable Long id) {

    User owner = getAuthenticatedUser(authentication);
    return contactService.getContact(id, owner);
  }

  @DeleteMapping("/{id}")
  public void delete(
      Authentication authentication,
      @PathVariable Long id) {

    User owner = getAuthenticatedUser(authentication);
    contactService.deleteContact(id, owner);
  }

  private User getAuthenticatedUser(Authentication authentication) {
    if (authentication == null) {
      throw new BadRequestException("Usuário autenticado não encontrado");
    }

    Object principal = authentication.getPrincipal();
    String username = null;

    if (principal instanceof UserDetails ud) {
      username = ud.getUsername();
    } else if (principal instanceof String s) {
      username = s;
    } else if (principal instanceof User u) {
      return (User) principal;
    }

    if (username == null || username.isBlank()) {
      throw new BadRequestException("Usuário autenticado não encontrado");
    }

    return userService.findEntityByEmail(username)
        .orElseThrow(() -> new BadRequestException("Usuário autenticado não encontrado"));
  }
}
