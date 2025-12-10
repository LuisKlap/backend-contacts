package com.dev.contacts.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;

@RestController
public class HealthCheckController {

  private final DataSource dataSource;
  private final Environment environment;
  private final Optional<Flyway> flyway;
  private final Optional<BuildProperties> buildProperties;
  private final Optional<GitProperties> gitProperties;

  @Value("${spring.application.name:contacts}")
  private String appName;

  @Value("${project.version:unknown}")
  private String projectVersion;

  public HealthCheckController(DataSource dataSource,
      Environment environment,
      ObjectProvider<Flyway> flywayProvider,
      ObjectProvider<BuildProperties> buildPropertiesProvider,
      ObjectProvider<GitProperties> gitPropertiesProvider) {
    this.dataSource = dataSource;
    this.environment = environment;
    this.flyway = Optional.ofNullable(flywayProvider.getIfAvailable());
    this.buildProperties = Optional.ofNullable(buildPropertiesProvider.getIfAvailable());
    this.gitProperties = Optional.ofNullable(gitPropertiesProvider.getIfAvailable());
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> body = new HashMap<>();
    body.put("status", "UP");
    body.put("service", appName);
    body.put("timestamp", OffsetDateTime.now().toString());

    String version = buildProperties.map(BuildProperties::getVersion)
        .or(() -> getImplementationVersion())
        .orElse(projectVersion);
    body.put("version", version);

    buildProperties.ifPresent(bp -> {
      body.put("buildTime", bp.getTime().toString());
      body.put("builtBy", bp.get("builtBy"));
    });

    gitProperties.ifPresent(gp -> {
      body.put("gitCommitId", gp.getCommitId());
      body.put("gitBranch", gp.getBranch());
    });

    body.put("javaVersion", System.getProperty("java.version"));
    body.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));

    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    body.put("uptimeMs", runtime.getUptime());
    body.put("startTime",
        OffsetDateTime.ofInstant(Instant.ofEpochMilli(runtime.getStartTime()), ZoneId.systemDefault()).toString());

    Map<String, Object> memory = new HashMap<>();
    Runtime rt = Runtime.getRuntime();
    memory.put("freeMemory", rt.freeMemory());
    memory.put("totalMemory", rt.totalMemory());
    memory.put("maxMemory", rt.maxMemory());
    body.put("memory", memory);

    body.put("threadCount", ManagementFactory.getThreadMXBean().getThreadCount());

    body.put("activeProfiles", environment.getActiveProfiles());

    Map<String, Object> db = new HashMap<>();
    try (Connection c = dataSource.getConnection()) {
      boolean valid = c.isValid(2);
      db.put("status", valid ? "UP" : "DEGRADED");
      db.put("catalog", c.getCatalog());
      db.put("schema", c.getSchema());
      db.put("databaseProductName", c.getMetaData().getDatabaseProductName());
      db.put("databaseProductVersion", c.getMetaData().getDatabaseProductVersion());
      String url = c.getMetaData().getURL();
      db.put("jdbcUrl", maskJdbcUrl(url));
    } catch (Exception ex) {
      db.put("status", "DOWN");
      db.put("error", ex.getMessage());
      body.put("status", "DEGRADED");
    }
    body.put("database", db);

    if (flyway.isPresent()) {
      Map<String, Object> fw = new HashMap<>();
      try {
        var info = flyway.get().info();
        fw.put("current",
            info.current() != null ? info.current().getVersion() + " - " + info.current().getDescription() : null);
        fw.put("pendingMigrations", info.pending().length);
        fw.put("appliedMigrations", info.applied().length);
        fw.put("success", true);
      } catch (Exception e) {
        fw.put("success", false);
        fw.put("error", e.getMessage());
      }
      body.put("flyway", fw);
    } else {
      body.put("flyway", Map.of("present", false));
    }

    return new ResponseEntity<>(body, HttpStatus.OK);
  }

  private Optional<String> getImplementationVersion() {
    Package pkg = this.getClass().getPackage();
    if (pkg != null && pkg.getImplementationVersion() != null) {
      return Optional.of(pkg.getImplementationVersion());
    }
    return Optional.empty();
  }

  private String maskJdbcUrl(String url) {
    if (url == null)
      return "unknown";
    try {
      String cleaned = url.replaceFirst("^jdbc:[a-zA-Z0-9]+://", "");
      cleaned = cleaned.replaceAll("^[^@/]+@", "");
      return cleaned;
    } catch (Exception e) {
      return "unknown";
    }
  }
}
