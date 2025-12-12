package com.dev.contacts.aspect;

import com.dev.contacts.config.RateLimitManager;
import com.dev.contacts.exception.OperationNotAllowedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Aspecto para aplicar rate limiting em métodos anotados.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

  private final RateLimitManager rateLimitManager;

  /**
   * Anotação para marcar métodos que devem ter rate limiting.
   */
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface RateLimit {
    /**
     * Se true, aplica limite mais restrito (3 req/hora).
     * Se false, aplica limite padrão (10 req/min).
     */
    boolean strict() default false;
  }

  @Around("@annotation(rateLimit)")
  public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

    if (attributes == null) {
      return joinPoint.proceed();
    }

    HttpServletRequest request = attributes.getRequest();
    String clientIp = rateLimitManager.getClientIp(request);
    String endpoint = request.getRequestURI();
    String key = clientIp + ":" + endpoint;

    boolean allowed = rateLimitManager.tryConsume(key, rateLimit.strict());

    if (!allowed) {
      log.warn("Rate limit exceeded for IP {} on endpoint {}", clientIp, endpoint);
      throw new OperationNotAllowedException(
          "Limite de requisições excedido. Tente novamente mais tarde.");
    }

    return joinPoint.proceed();
  }
}
