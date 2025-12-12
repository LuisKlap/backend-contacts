package com.dev.contacts.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerenciador de rate limiting para proteção contra abuso de endpoints.
 * 
 * Implementa algoritmo Token Bucket para limitar requisições por IP.
 */
@Slf4j
@Component
public class RateLimitManager {

  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  /**
   * Cria um bucket para rate limiting padrão (10 req/min).
   */
  private Bucket createDefaultBucket() {
    Bandwidth limit = Bandwidth.builder()
        .capacity(10)
        .refillIntervally(10, Duration.ofMinutes(1))
        .build();
    return Bucket.builder()
        .addLimit(limit)
        .build();
  }

  /**
   * Cria um bucket para endpoints sensíveis (3 req/hora).
   */
  private Bucket createStrictBucket() {
    Bandwidth limit = Bandwidth.builder()
        .capacity(3)
        .refillIntervally(3, Duration.ofHours(1))
        .build();
    return Bucket.builder()
        .addLimit(limit)
        .build();
  }

  /**
   * Resolve o bucket baseado no IP do cliente.
   */
  public Bucket resolveBucket(String key, boolean strict) {
    return cache.computeIfAbsent(key, k -> strict ? createStrictBucket() : createDefaultBucket());
  }

  /**
   * Tenta consumir um token do bucket.
   * 
   * @return true se permitido, false se limite excedido
   */
  public boolean tryConsume(String key, boolean strict) {
    Bucket bucket = resolveBucket(key, strict);
    boolean allowed = bucket.tryConsume(1);

    if (!allowed) {
      log.warn("Rate limit exceeded for key: {}", key);
    }

    return allowed;
  }

  /**
   * Extrai o IP do cliente da requisição.
   */
  public String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
