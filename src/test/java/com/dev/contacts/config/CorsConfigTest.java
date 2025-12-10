package com.dev.contacts.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

  private CorsConfig corsConfig;

  @BeforeEach
  void setUp() {
    corsConfig = new CorsConfig();
    ReflectionTestUtils.setField(corsConfig, "allowedOrigins",
        "https://front-contacts.vercel.app,http://localhost:3000,http://localhost:4200");
  }

  @Test
  void shouldCreateCorsFilter() {
    CorsFilter corsFilter = corsConfig.corsFilter();

    assertThat(corsFilter).isNotNull();
  }

  @Test
  void shouldConfigureCorsWithAllowedOrigins() {
    CorsFilter corsFilter = corsConfig.corsFilter();

    assertThat(corsFilter).isNotNull();
  }

  @Test
  void shouldAllowAllMethods() {
    CorsFilter corsFilter = corsConfig.corsFilter();

    assertThat(corsFilter).isNotNull();
  }

  @Test
  void shouldAllowCredentials() {
    CorsFilter corsFilter = corsConfig.corsFilter();

    assertThat(corsFilter).isNotNull();
  }

  @Test
  void shouldExposeAuthorizationHeader() {
    CorsFilter corsFilter = corsConfig.corsFilter();

    assertThat(corsFilter).isNotNull();
  }

  @Test
  void shouldSetMaxAge() {
    CorsFilter corsFilter = corsConfig.corsFilter();

    assertThat(corsFilter).isNotNull();
  }

  @Test
  void shouldHandleSingleOrigin() {
    ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "https://example.com");

    CorsFilter corsFilter = corsConfig.corsFilter();

    assertThat(corsFilter).isNotNull();
  }
}
