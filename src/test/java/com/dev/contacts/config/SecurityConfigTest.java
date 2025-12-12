package com.dev.contacts.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

  @Mock
  private HttpSecurity httpSecurity;

  @Mock
  private JwtAuthFilter jwtAuthFilter;

  @Mock
  private AuthenticationConfiguration authenticationConfiguration;

  @InjectMocks
  private SecurityConfig securityConfig;

  @Test
  void shouldCreatePasswordEncoder() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

    assertThat(passwordEncoder).isNotNull();
    assertThat(passwordEncoder.getClass().getSimpleName()).isEqualTo("BCryptPasswordEncoder");
  }

  @Test
  void shouldEncodePassword() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "myPassword123";

    String encodedPassword = passwordEncoder.encode(rawPassword);

    assertThat(encodedPassword).isNotNull();
    assertThat(encodedPassword).isNotEqualTo(rawPassword);
    assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
  }

  @Test
  void shouldCreateAuthenticationManager() throws Exception {
    AuthenticationManager mockManager = mock(AuthenticationManager.class);
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);

    AuthenticationManager authenticationManager = securityConfig.authenticationManager(authenticationConfiguration);

    assertThat(authenticationManager).isNotNull();
    assertThat(authenticationManager).isEqualTo(mockManager);
  }

  @Test
  void shouldCreateCorsConfigurationSource() {
    CorsConfigurationSource corsConfigurationSource = securityConfig.corsConfigurationSource();

    assertThat(corsConfigurationSource).isNotNull();
  }

  @Test
  void shouldNotMatchDifferentPasswords() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "myPassword123";
    String wrongPassword = "wrongPassword";

    String encodedPassword = passwordEncoder.encode(rawPassword);

    assertThat(passwordEncoder.matches(wrongPassword, encodedPassword)).isFalse();
  }

  @Test
  void shouldGenerateDifferentHashesForSamePassword() {
    PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
    String rawPassword = "myPassword123";

    String encodedPassword1 = passwordEncoder.encode(rawPassword);
    String encodedPassword2 = passwordEncoder.encode(rawPassword);

    assertThat(encodedPassword1).isNotEqualTo(encodedPassword2);
    assertThat(passwordEncoder.matches(rawPassword, encodedPassword1)).isTrue();
    assertThat(passwordEncoder.matches(rawPassword, encodedPassword2)).isTrue();
  }
}
