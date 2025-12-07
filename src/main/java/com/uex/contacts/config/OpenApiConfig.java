package com.uex.contacts.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI contactsOpenAPI() {
    final String securitySchemeName = "bearerAuth";

    String serverUrl = resolveServerUrl();

    Server server = new Server()
        .url(serverUrl)
        .description("API Contacts");

    return new OpenAPI()
        .info(new Info()
            .title("Contacts API")
            .version("v1")
            .description("API para o teste UEX - cadastro de contatos"))
        .addServersItem(server)
        .components(new Components()
            .addSecuritySchemes(securitySchemeName,
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .in(SecurityScheme.In.HEADER)
                    .name("Authorization")))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
  }

  private String resolveServerUrl() {
    String railwayDomain = System.getenv("RAILWAY_PUBLIC_DOMAIN");

    if (railwayDomain != null && !railwayDomain.isBlank()) {
      return "https://" + railwayDomain;
    }

    return "http://localhost:8080";
  }
}
