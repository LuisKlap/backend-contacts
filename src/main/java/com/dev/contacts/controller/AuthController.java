package com.dev.contacts.controller;

import com.dev.contacts.aspect.RateLimitAspect.RateLimit;
import com.dev.contacts.dto.auth.AuthResponse;
import com.dev.contacts.dto.auth.ForgotPasswordRequest;
import com.dev.contacts.dto.auth.ForgotPasswordResponse;
import com.dev.contacts.dto.auth.LoginRequest;
import com.dev.contacts.dto.auth.LogoutRequest;
import com.dev.contacts.dto.auth.RefreshTokenRequest;
import com.dev.contacts.dto.auth.ResetPasswordRequest;
import com.dev.contacts.dto.auth.ResetPasswordResponse;
import com.dev.contacts.dto.auth.SignupRequest;
import com.dev.contacts.service.AuthService;
import com.dev.contacts.service.PasswordResetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável por endpoints de autenticação e autorização.
 * 
 * Gerencia registro, login, logout, refresh de tokens e recuperação de senha.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints de autenticação e autorização")
public class AuthController {

  private final AuthService authService;
  private final PasswordResetService passwordResetService;

  @PostMapping("/signup")
  @RateLimit(strict = false)
  @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário no sistema")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
      @ApiResponse(responseCode = "400", description = "Dados inválidos"),
      @ApiResponse(responseCode = "409", description = "Email já cadastrado"),
      @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
  })
  public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
    log.debug("Signup request received for email: {}", request.email());
    authService.signup(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PostMapping("/login")
  @RateLimit(strict = false)
  @Operation(summary = "Autenticar usuário", description = "Realiza login e retorna tokens de acesso")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
      @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
      @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
  })
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    log.debug("Login request received for email: {}", request.email());
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Renovar token de acesso", description = "Gera novo token de acesso usando refresh token válido")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
      @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
  })
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    log.debug("Token refresh request received");
    AuthResponse response = authService.refreshToken(request.refreshToken());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  @Operation(summary = "Encerrar sessão", description = "Revoga o refresh token e encerra a sessão do usuário")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso")
  })
  public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
    log.debug("Logout request received");
    authService.logout(request.refreshToken());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/forgot-password")
  @RateLimit(strict = true)
  @Operation(summary = "Solicitar recuperação de senha", description = "Envia email com link para redefinição de senha se o email existir")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Solicitação processada"),
      @ApiResponse(responseCode = "429", description = "Limite de requisições excedido (3 por hora)")
  })
  public ResponseEntity<ForgotPasswordResponse> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    log.debug("Password reset request received for email: {}", request.email());
    ForgotPasswordResponse response = passwordResetService.requestPasswordReset(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/reset-password")
  @RateLimit(strict = false)
  @Operation(summary = "Redefinir senha", description = "Redefine a senha usando token válido recebido por email")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Senha redefinida com sucesso"),
      @ApiResponse(responseCode = "400", description = "Token inválido ou expirado"),
      @ApiResponse(responseCode = "429", description = "Limite de requisições excedido")
  })
  public ResponseEntity<ResetPasswordResponse> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    log.debug("Password reset execution request received");
    ResetPasswordResponse response = passwordResetService.resetPassword(request);
    return ResponseEntity.ok(response);
  }
}
