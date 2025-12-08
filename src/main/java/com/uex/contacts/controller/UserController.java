package com.uex.contacts.controller;

import com.uex.contacts.dto.user.UpdateUserRequest;
import com.uex.contacts.dto.user.UserResponse;
import com.uex.contacts.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userService;

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

  @PutMapping
  public ResponseEntity<UserResponse> updateCurrentUser(@Valid @RequestBody UpdateUserRequest request) {
    UserResponse response = userService.updateCurrentUser(request);
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
