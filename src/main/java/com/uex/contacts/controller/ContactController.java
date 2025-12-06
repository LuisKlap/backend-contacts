package com.uex.contacts.controller;

import com.uex.contacts.dto.contact.ContactRequest;
import com.uex.contacts.dto.contact.ContactResponse;
import com.uex.contacts.entity.User;
import com.uex.contacts.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
  private final ContactService contactService;

  @PostMapping
  public ContactResponse create(
      @AuthenticationPrincipal User authenticatedUser,
      @Valid @RequestBody ContactRequest request) {

    return contactService.createContact(authenticatedUser, request);
  }

  @PutMapping("/{id}")
  public ContactResponse update(
      @AuthenticationPrincipal User authenticatedUser,
      @PathVariable Long id,
      @Valid @RequestBody ContactRequest request) {

    return contactService.updateContact(id, authenticatedUser, request);
  }

  @GetMapping
  public Page<ContactResponse> list(
      @AuthenticationPrincipal User authenticatedUser,
      @RequestParam(required = false) String filter,
      Pageable pageable) {

    return contactService.listContacts(authenticatedUser, filter, pageable);
  }

  @GetMapping("/{id}")
  public ContactResponse getOne(
      @AuthenticationPrincipal User authenticatedUser,
      @PathVariable Long id) {

    return contactService.getContact(id, authenticatedUser);
  }

  @DeleteMapping("/{id}")
  public void delete(
      @AuthenticationPrincipal User authenticatedUser,
      @PathVariable Long id) {

    contactService.deleteContact(id, authenticatedUser);
  }

}
