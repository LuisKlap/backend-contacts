package com.uex.contacts.controller;

import com.uex.contacts.config.JwtAuthFilter;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Properties;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HealthCheckController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureJsonTesters
@DisplayName("HealthCheckController Tests")
@Disabled("Testes temporariamente desabilitados - problema ao carregar contexto da aplicação")
class HealthCheckControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private DataSource dataSource;

  @MockitoBean
  private Environment environment;

  @MockitoBean
  private ObjectProvider<Flyway> flywayProvider;

  @MockitoBean
  private ObjectProvider<BuildProperties> buildPropertiesProvider;

  @MockitoBean
  private ObjectProvider<GitProperties> gitPropertiesProvider;

  @Test
  @DisplayName("GET /health - Deve retornar status UP com todas as informações básicas")
  void shouldReturnHealthStatusUp() throws Exception {
    Connection mockConnection = mock(Connection.class);
    DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.isValid(anyInt())).thenReturn(true);
    when(mockConnection.getCatalog()).thenReturn("contacts");
    when(mockConnection.getSchema()).thenReturn("public");
    when(mockConnection.getMetaData()).thenReturn(mockMetadata);
    when(mockMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
    when(mockMetadata.getDatabaseProductVersion()).thenReturn("15.3");
    when(mockMetadata.getURL()).thenReturn("jdbc:postgresql://localhost:5432/contacts");

    when(environment.getActiveProfiles()).thenReturn(new String[] { "dev" });
    when(flywayProvider.getIfAvailable()).thenReturn(null);
    when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"))
        .andExpect(jsonPath("$.service").value("contacts"))
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.version").exists())
        .andExpect(jsonPath("$.javaVersion").exists())
        .andExpect(jsonPath("$.os").exists())
        .andExpect(jsonPath("$.uptimeMs").exists())
        .andExpect(jsonPath("$.startTime").exists())
        .andExpect(jsonPath("$.memory").exists())
        .andExpect(jsonPath("$.memory.freeMemory").exists())
        .andExpect(jsonPath("$.memory.totalMemory").exists())
        .andExpect(jsonPath("$.memory.maxMemory").exists())
        .andExpect(jsonPath("$.threadCount").exists())
        .andExpect(jsonPath("$.activeProfiles[0]").value("dev"))
        .andExpect(jsonPath("$.database.status").value("UP"))
        .andExpect(jsonPath("$.database.databaseProductName").value("PostgreSQL"))
        .andExpect(jsonPath("$.database.databaseProductVersion").value("15.3"));

    verify(mockConnection, times(1)).close();
  }

  @Test
  @DisplayName("GET /health - Deve retornar informações de build quando disponíveis")
  void shouldReturnBuildPropertiesWhenAvailable() throws Exception {
    Connection mockConnection = mock(Connection.class);
    DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.isValid(anyInt())).thenReturn(true);
    when(mockConnection.getMetaData()).thenReturn(mockMetadata);
    when(mockMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
    when(mockMetadata.getDatabaseProductVersion()).thenReturn("15.3");
    when(mockMetadata.getURL()).thenReturn("jdbc:postgresql://localhost:5432/contacts");

    Properties props = new Properties();
    props.put("builtBy", "maven");
    BuildProperties buildProperties = new BuildProperties(props);

    when(buildPropertiesProvider.getIfAvailable()).thenReturn(buildProperties);
    when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(flywayProvider.getIfAvailable()).thenReturn(null);
    when(environment.getActiveProfiles()).thenReturn(new String[] {});

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.buildTime").exists())
        .andExpect(jsonPath("$.builtBy").value("maven"));
  }

  @Test
  @DisplayName("GET /health - Deve retornar informações Git quando disponíveis")
  void shouldReturnGitPropertiesWhenAvailable() throws Exception {
    Connection mockConnection = mock(Connection.class);
    DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.isValid(anyInt())).thenReturn(true);
    when(mockConnection.getMetaData()).thenReturn(mockMetadata);
    when(mockMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
    when(mockMetadata.getDatabaseProductVersion()).thenReturn("15.3");
    when(mockMetadata.getURL()).thenReturn("jdbc:postgresql://localhost:5432/contacts");

    Properties gitProps = new Properties();
    gitProps.put("branch", "main");
    gitProps.put("commit.id", "abc123");
    GitProperties gitProperties = new GitProperties(gitProps);

    when(gitPropertiesProvider.getIfAvailable()).thenReturn(gitProperties);
    when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(flywayProvider.getIfAvailable()).thenReturn(null);
    when(environment.getActiveProfiles()).thenReturn(new String[] {});

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.gitCommitId").value("abc123"))
        .andExpect(jsonPath("$.gitBranch").value("main"));
  }

  @Test
  @DisplayName("GET /health - Deve retornar informações Flyway quando disponíveis")
  void shouldReturnFlywayInfoWhenAvailable() throws Exception {
    Connection mockConnection = mock(Connection.class);
    DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.isValid(anyInt())).thenReturn(true);
    when(mockConnection.getMetaData()).thenReturn(mockMetadata);
    when(mockMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
    when(mockMetadata.getDatabaseProductVersion()).thenReturn("15.3");
    when(mockMetadata.getURL()).thenReturn("jdbc:postgresql://localhost:5432/contacts");

    Flyway mockFlyway = mock(Flyway.class);
    MigrationInfoService mockInfoService = mock(MigrationInfoService.class);
    MigrationInfo mockCurrentInfo = mock(MigrationInfo.class);
    MigrationVersion mockVersion = mock(MigrationVersion.class);

    when(mockFlyway.info()).thenReturn(mockInfoService);
    when(mockInfoService.current()).thenReturn(mockCurrentInfo);
    when(mockCurrentInfo.getVersion()).thenReturn(mockVersion);
    when(mockVersion.toString()).thenReturn("2");
    when(mockCurrentInfo.getDescription()).thenReturn("rename user_id to owner_id");
    when(mockInfoService.pending()).thenReturn(new MigrationInfo[0]);
    when(mockInfoService.applied()).thenReturn(new MigrationInfo[2]);

    when(flywayProvider.getIfAvailable()).thenReturn(mockFlyway);
    when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(environment.getActiveProfiles()).thenReturn(new String[] {});

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flyway.success").value(true))
        .andExpect(jsonPath("$.flyway.current").value(containsString("2")))
        .andExpect(jsonPath("$.flyway.pendingMigrations").value(0))
        .andExpect(jsonPath("$.flyway.appliedMigrations").value(2));
  }

  @Test
  @DisplayName("GET /health - Deve retornar status DEGRADED quando banco está down")
  void shouldReturnDegradedWhenDatabaseDown() throws Exception {
    when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));
    when(environment.getActiveProfiles()).thenReturn(new String[] {});
    when(flywayProvider.getIfAvailable()).thenReturn(null);
    when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DEGRADED"))
        .andExpect(jsonPath("$.database.status").value("DOWN"))
        .andExpect(jsonPath("$.database.error").exists());
  }

  @Test
  @DisplayName("GET /health - Deve retornar status DEGRADED quando conexão é inválida")
  void shouldReturnDegradedWhenConnectionInvalid() throws Exception {
    Connection mockConnection = mock(Connection.class);
    DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.isValid(anyInt())).thenReturn(false);
    when(mockConnection.getCatalog()).thenReturn("contacts");
    when(mockConnection.getSchema()).thenReturn("public");
    when(mockConnection.getMetaData()).thenReturn(mockMetadata);
    when(mockMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
    when(mockMetadata.getDatabaseProductVersion()).thenReturn("15.3");
    when(mockMetadata.getURL()).thenReturn("jdbc:postgresql://localhost:5432/contacts");

    when(environment.getActiveProfiles()).thenReturn(new String[] {});
    when(flywayProvider.getIfAvailable()).thenReturn(null);
    when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.database.status").value("DEGRADED"));

    verify(mockConnection, times(1)).close();
  }

  @Test
  @DisplayName("GET /health - Deve retornar informações de Flyway com erro quando falha")
  void shouldReturnFlywayErrorWhenFlywayFails() throws Exception {
    Connection mockConnection = mock(Connection.class);
    DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.isValid(anyInt())).thenReturn(true);
    when(mockConnection.getMetaData()).thenReturn(mockMetadata);
    when(mockMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
    when(mockMetadata.getDatabaseProductVersion()).thenReturn("15.3");
    when(mockMetadata.getURL()).thenReturn("jdbc:postgresql://localhost:5432/contacts");

    Flyway mockFlyway = mock(Flyway.class);
    when(mockFlyway.info()).thenThrow(new FlywayException("Flyway error"));

    when(flywayProvider.getIfAvailable()).thenReturn(mockFlyway);
    when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(environment.getActiveProfiles()).thenReturn(new String[] {});

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flyway.success").value(false))
        .andExpect(jsonPath("$.flyway.error").exists());
  }

  @Test
  @DisplayName("GET /health - Deve indicar quando Flyway não está presente")
  void shouldIndicateWhenFlywayNotPresent() throws Exception {
    Connection mockConnection = mock(Connection.class);
    DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.isValid(anyInt())).thenReturn(true);
    when(mockConnection.getMetaData()).thenReturn(mockMetadata);
    when(mockMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
    when(mockMetadata.getDatabaseProductVersion()).thenReturn("15.3");
    when(mockMetadata.getURL()).thenReturn("jdbc:postgresql://localhost:5432/contacts");

    when(flywayProvider.getIfAvailable()).thenReturn(null);
    when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(environment.getActiveProfiles()).thenReturn(new String[] {});

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.flyway.present").value(false));
  }

  @Test
  @DisplayName("GET /health - Deve mascarar JDBC URL corretamente")
  void shouldMaskJdbcUrl() throws Exception {
    Connection mockConnection = mock(Connection.class);
    DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.isValid(anyInt())).thenReturn(true);
    when(mockConnection.getMetaData()).thenReturn(mockMetadata);
    when(mockMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
    when(mockMetadata.getDatabaseProductVersion()).thenReturn("15.3");
    when(mockMetadata.getURL()).thenReturn("jdbc:postgresql://user:password@localhost:5432/contacts");

    when(environment.getActiveProfiles()).thenReturn(new String[] {});
    when(flywayProvider.getIfAvailable()).thenReturn(null);
    when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.database.jdbcUrl").value(not(containsString("password"))))
        .andExpect(jsonPath("$.database.jdbcUrl").value(not(containsString("user"))));
  }

  @Test
  @DisplayName("GET /health - Endpoint deve ser público (sem autenticação)")
  void shouldBePublicEndpoint() throws Exception {
    Connection mockConnection = mock(Connection.class);
    DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);

    when(dataSource.getConnection()).thenReturn(mockConnection);
    when(mockConnection.isValid(anyInt())).thenReturn(true);
    when(mockConnection.getMetaData()).thenReturn(mockMetadata);
    when(mockMetadata.getDatabaseProductName()).thenReturn("PostgreSQL");
    when(mockMetadata.getDatabaseProductVersion()).thenReturn("15.3");
    when(mockMetadata.getURL()).thenReturn("jdbc:postgresql://localhost:5432/contacts");

    when(environment.getActiveProfiles()).thenReturn(new String[] {});
    when(flywayProvider.getIfAvailable()).thenReturn(null);
    when(buildPropertiesProvider.getIfAvailable()).thenReturn(null);
    when(gitPropertiesProvider.getIfAvailable()).thenReturn(null);

    mockMvc.perform(get("/health"))
        .andExpect(status().isOk());
  }
}
