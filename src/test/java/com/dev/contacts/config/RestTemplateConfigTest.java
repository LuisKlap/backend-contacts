package com.dev.contacts.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RestTemplateConfigTest {

  private final RestTemplateConfig restTemplateConfig = new RestTemplateConfig();

  @Test
  void shouldCreateRestTemplate() {
    RestTemplate restTemplate = restTemplateConfig.restTemplate();

    assertThat(restTemplate).isNotNull();
  }

  @Test
  void shouldConfigureRequestFactory() {
    RestTemplate restTemplate = restTemplateConfig.restTemplate();

    assertThat(restTemplate.getRequestFactory()).isInstanceOf(SimpleClientHttpRequestFactory.class);
  }

  @Test
  void shouldConfigureSimpleClientHttpRequestFactory() {
    RestTemplate restTemplate = restTemplateConfig.restTemplate();

    SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    assertThat(factory).isNotNull();
  }

  @Test
  void shouldReturnNewInstanceEachTime() {
    RestTemplate restTemplate1 = restTemplateConfig.restTemplate();
    RestTemplate restTemplate2 = restTemplateConfig.restTemplate();

    assertThat(restTemplate1).isNotSameAs(restTemplate2);
  }

  @Test
  void shouldConfigureRestTemplateWithInterceptors() {
    RestTemplate restTemplate = restTemplateConfig.restTemplate();

    assertThat(restTemplate.getInterceptors()).isNotNull();
  }
}
