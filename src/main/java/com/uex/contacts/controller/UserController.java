package com.uex.contacts.controller;

import com.uex.contacts.dto.user.UserResponse;
import com.uex.contacts.entity.User;
import com.uex.contacts.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    User user = User.builder()
        .fullName(request.fullName())
        .email(request.email())
        .build();

    UserResponse response = userService.createUser(user, request.password());

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(response.id())
        .toUri();

    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser() {
    UserResponse response = userService.findCurrentUser();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
    UserResponse response = userService.findById(id);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteCurrentUser(@Valid @RequestBody DeleteAccountRequest request) {
    userService.deleteCurrentUser(request.password());
    return ResponseEntity.noContent().build();
  }

  public static record CreateUserRequest(
      @NotBlank(message = "fullName é obrigatório") String fullName,
      @NotBlank(message = "email é obrigatório") @Email(message = "email inválido") String email,
      @NotBlank(message = "password é obrigatório") String password) {
  }

  public static record DeleteAccountRequest(
      @NotBlank(message = "password é obrigatório") String password) {
  }
}
