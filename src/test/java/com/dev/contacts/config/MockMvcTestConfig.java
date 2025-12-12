package com.dev.contacts.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuração de teste que adiciona um resolver customizado para o parâmetro
 * Authentication.
 * Necessário porque @WebMvcTest não inclui o resolver padrão do Spring
 * Security.
 */
@TestConfiguration
public class MockMvcTestConfig implements InitializingBean {

  @Autowired
  private RequestMappingHandlerAdapter adapter;

  @Override
  public void afterPropertiesSet() {
    List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(adapter.getArgumentResolvers());
    resolvers.add(0, new AuthenticationArgumentResolver());
    adapter.setArgumentResolvers(resolvers);
  }

  private static class AuthenticationArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
      return Authentication.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
      // Tenta obter o Authentication do SecurityContext primeiro
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();

      // Se não encontrar, busca no atributo onde SecurityMockMvcRequestPostProcessors
      // armazena
      if (auth == null) {
        // O SecurityMockMvcRequestPostProcessors armazena o SecurityContext em um
        // atributo específico
        String attrName = "org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors$SecurityContextRequestPostProcessorSupport$TestSecurityContextRepository.REPO";
        Object securityContext = webRequest.getAttribute(attrName, NativeWebRequest.SCOPE_REQUEST);

        if (securityContext instanceof org.springframework.security.core.context.SecurityContext sc) {
          auth = sc.getAuthentication();
        }
      }

      return auth;
    }
  }
}
