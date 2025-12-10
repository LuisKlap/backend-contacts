package com.dev.contacts.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

  private final OpenApiConfig openApiConfig = new OpenApiConfig();

  @AfterEach
  void cleanup() {
    // Clear environment variable
    System.clearProperty("RAILWAY_PUBLIC_DOMAIN");
  }

  @Test
  void shouldCreateOpenAPIConfiguration() {
    OpenAPI openAPI = openApiConfig.contactsOpenAPI();

    assertThat(openAPI).isNotNull();
  }

  @Test
  void shouldConfigureInfoWithCorrectTitle() {
    OpenAPI openAPI = openApiConfig.contactsOpenAPI();

    Info info = openAPI.getInfo();
    assertThat(info).isNotNull();
    assertThat(info.getTitle()).isEqualTo("Contacts API");
  }

  @Test
  void shouldConfigureInfoWithCorrectVersion() {
    OpenAPI openAPI = openApiConfig.contactsOpenAPI();

    Info info = openAPI.getInfo();
    assertThat(info.getVersion()).isEqualTo("v1");
  }

  @Test
  void shouldConfigureInfoWithDescription() {
    OpenAPI openAPI = openApiConfig.contactsOpenAPI();

    Info info = openAPI.getInfo();
    assertThat(info.getDescription()).contains("API para o teste dev");
  }

  @Test
  void shouldConfigureServerWithLocalhostByDefault() {
    OpenAPI openAPI = openApiConfig.contactsOpenAPI();

    assertThat(openAPI.getServers()).isNotNull();
    assertThat(openAPI.getServers()).hasSize(1);

    Server server = openAPI.getServers().get(0);
    assertThat(server.getUrl()).isEqualTo("http://localhost:8080");
    assertThat(server.getDescription()).isEqualTo("API Contacts");
  }

  @Test
  void shouldConfigureBearerAuthSecurityScheme() {
    OpenAPI openAPI = openApiConfig.contactsOpenAPI();

    assertThat(openAPI.getComponents()).isNotNull();
    assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");

    SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
    assertThat(securityScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
    assertThat(securityScheme.getScheme()).isEqualTo("bearer");
    assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
    assertThat(securityScheme.getIn()).isEqualTo(SecurityScheme.In.HEADER);
    assertThat(securityScheme.getName()).isEqualTo("Authorization");
  }

  @Test
  void shouldAddSecurityRequirement() {
    OpenAPI openAPI = openApiConfig.contactsOpenAPI();

    assertThat(openAPI.getSecurity()).isNotNull();
    assertThat(openAPI.getSecurity()).hasSize(1);

    SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
    assertThat(securityRequirement.get("bearerAuth")).isNotNull();
  }
}
