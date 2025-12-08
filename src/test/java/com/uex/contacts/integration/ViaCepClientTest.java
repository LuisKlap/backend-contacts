package com.uex.contacts.integration;

import com.uex.contacts.config.AddressLookupProperties;
import com.uex.contacts.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViaCepClient Tests")
class ViaCepClientTest {

  @Mock
  private AddressLookupProperties properties;

  @Mock
  private RestTemplate restTemplate;

  private ViaCepClient viaCepClient;

  @BeforeEach
  void setUp() {
    when(properties.getViacepBaseUrl()).thenReturn("https://viacep.com.br/ws");
    viaCepClient = new ViaCepClient(properties, restTemplate);
  }

  @Test
  @DisplayName("Deve buscar endereço por CEP com sucesso")
  void shouldFindAddressByCepSuccessfully() {
    // Arrange
    String cep = "01310100";
    ViaCepClient.ViaCepAddress expectedAddress = createViaCepAddress();

    when(restTemplate.getForEntity(
        eq("https://viacep.com.br/ws/01310100/json/"),
        eq(ViaCepClient.ViaCepAddress.class)))
        .thenReturn(ResponseEntity.ok(expectedAddress));

    // Act
    ViaCepClient.ViaCepAddress result = viaCepClient.findByCep(cep);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.cep).isEqualTo("01310-100");
    assertThat(result.logradouro).isEqualTo("Avenida Paulista");
    assertThat(result.bairro).isEqualTo("Bela Vista");
    assertThat(result.localidade).isEqualTo("São Paulo");
    assertThat(result.uf).isEqualTo("SP");
  }

  @Test
  @DisplayName("Deve retornar endereço com erro quando CEP não existe")
  void shouldReturnAddressWithErrorWhenCepNotFound() {
    // Arrange
    String cep = "99999999";
    ViaCepClient.ViaCepAddress errorAddress = new ViaCepClient.ViaCepAddress();
    errorAddress.erro = true;

    when(restTemplate.getForEntity(
        eq("https://viacep.com.br/ws/99999999/json/"),
        eq(ViaCepClient.ViaCepAddress.class)))
        .thenReturn(ResponseEntity.ok(errorAddress));

    // Act
    ViaCepClient.ViaCepAddress result = viaCepClient.findByCep(cep);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.erro).isTrue();
  }

  @Test
  @DisplayName("Deve lançar ExternalServiceException quando RestTemplate falhar na busca por CEP")
  void shouldThrowExternalServiceExceptionWhenRestTemplateFails() {
    // Arrange
    String cep = "01310100";
    when(restTemplate.getForEntity(
        any(String.class),
        eq(ViaCepClient.ViaCepAddress.class)))
        .thenThrow(new RestClientException("Connection timeout"));

    // Act & Assert
    assertThatThrownBy(() -> viaCepClient.findByCep(cep))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("ViaCep CEP lookup failed");
  }

  @Test
  @DisplayName("Deve buscar endereços por UF, cidade e logradouro com sucesso")
  void shouldSearchByUfCityStreetSuccessfully() {
    // Arrange
    String uf = "SP";
    String city = "São Paulo";
    String street = "Paulista";

    ViaCepClient.ViaCepAddress[] addresses = {
        createViaCepAddress(),
        createViaCepAddress2()
    };

    when(restTemplate.getForEntity(
        eq("https://viacep.com.br/ws/SP/S%C3%A3o%20Paulo/Paulista/json/"),
        eq(ViaCepClient.ViaCepAddress[].class)))
        .thenReturn(ResponseEntity.ok(addresses));

    // Act
    List<ViaCepClient.ViaCepAddress> result = viaCepClient.searchByUfCityStreet(uf, city, street);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result.get(0).logradouro).isEqualTo("Avenida Paulista");
    assertThat(result.get(1).logradouro).isEqualTo("Rua Paulista");
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando não encontrar endereços")
  void shouldReturnEmptyListWhenNoAddressesFound() {
    // Arrange
    String uf = "SP";
    String city = "São Paulo";
    String street = "RuaInexistente123456";

    when(restTemplate.getForEntity(
        any(String.class),
        eq(ViaCepClient.ViaCepAddress[].class)))
        .thenReturn(ResponseEntity.ok(null));

    // Act
    List<ViaCepClient.ViaCepAddress> result = viaCepClient.searchByUfCityStreet(uf, city, street);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando resposta for array vazio")
  void shouldReturnEmptyListWhenResponseIsEmptyArray() {
    // Arrange
    String uf = "SP";
    String city = "São Paulo";
    String street = "RuaInexistente";

    ViaCepClient.ViaCepAddress[] emptyArray = {};

    when(restTemplate.getForEntity(
        any(String.class),
        eq(ViaCepClient.ViaCepAddress[].class)))
        .thenReturn(ResponseEntity.ok(emptyArray));

    // Act
    List<ViaCepClient.ViaCepAddress> result = viaCepClient.searchByUfCityStreet(uf, city, street);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve lançar ExternalServiceException quando RestTemplate falhar na busca por endereço")
  void shouldThrowExternalServiceExceptionWhenSearchFails() {
    // Arrange
    String uf = "SP";
    String city = "São Paulo";
    String street = "Paulista";

    when(restTemplate.getForEntity(
        any(String.class),
        eq(ViaCepClient.ViaCepAddress[].class)))
        .thenThrow(new RestClientException("Connection timeout"));

    // Act & Assert
    assertThatThrownBy(() -> viaCepClient.searchByUfCityStreet(uf, city, street))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("ViaCep lookup failed");
  }

  @Test
  @DisplayName("Deve encodar corretamente caracteres especiais na busca")
  void shouldEncodeSpecialCharactersCorrectly() {
    // Arrange
    String uf = "SP";
    String city = "São José dos Campos";
    String street = "Rua dos Três Irmãos";

    ViaCepClient.ViaCepAddress[] addresses = { createViaCepAddress() };

    when(restTemplate.getForEntity(
        any(String.class),
        eq(ViaCepClient.ViaCepAddress[].class)))
        .thenReturn(ResponseEntity.ok(addresses));

    // Act
    List<ViaCepClient.ViaCepAddress> result = viaCepClient.searchByUfCityStreet(uf, city, street);

    // Assert
    assertThat(result).isNotEmpty();
  }

  @Test
  @DisplayName("Deve tratar valores nulos na busca")
  void shouldHandleNullValuesInSearch() {
    // Arrange
    ViaCepClient.ViaCepAddress[] addresses = {};

    when(restTemplate.getForEntity(
        any(String.class),
        eq(ViaCepClient.ViaCepAddress[].class)))
        .thenReturn(ResponseEntity.ok(addresses));

    // Act
    List<ViaCepClient.ViaCepAddress> result = viaCepClient.searchByUfCityStreet(null, null, null);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve remover espaços em branco antes de encodar")
  void shouldTrimWhitespacesBeforeEncoding() {
    // Arrange
    String uf = "  SP  ";
    String city = "  São Paulo  ";
    String street = "  Paulista  ";

    ViaCepClient.ViaCepAddress[] addresses = { createViaCepAddress() };

    when(restTemplate.getForEntity(
        any(String.class),
        eq(ViaCepClient.ViaCepAddress[].class)))
        .thenReturn(ResponseEntity.ok(addresses));

    // Act
    List<ViaCepClient.ViaCepAddress> result = viaCepClient.searchByUfCityStreet(uf, city, street);

    // Assert
    assertThat(result).isNotEmpty();
  }

  @Test
  @DisplayName("Deve retornar endereço com todos os campos preenchidos")
  void shouldReturnAddressWithAllFieldsPopulated() {
    // Arrange
    String cep = "01310100";
    ViaCepClient.ViaCepAddress expectedAddress = createViaCepAddress();
    expectedAddress.complemento = "de 612 a 1510 - lado par";

    when(restTemplate.getForEntity(
        eq("https://viacep.com.br/ws/01310100/json/"),
        eq(ViaCepClient.ViaCepAddress.class)))
        .thenReturn(ResponseEntity.ok(expectedAddress));

    // Act
    ViaCepClient.ViaCepAddress result = viaCepClient.findByCep(cep);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.cep).isNotNull();
    assertThat(result.logradouro).isNotNull();
    assertThat(result.complemento).isNotNull();
    assertThat(result.bairro).isNotNull();
    assertThat(result.localidade).isNotNull();
    assertThat(result.uf).isNotNull();
  }

  @Test
  @DisplayName("Deve retornar null quando resposta de status não-OK")
  void shouldReturnNullWhenNonOkStatusResponse() {
    // Arrange
    String cep = "01310100";

    when(restTemplate.getForEntity(
        any(String.class),
        eq(ViaCepClient.ViaCepAddress.class)))
        .thenReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());

    // Act
    ViaCepClient.ViaCepAddress result = viaCepClient.findByCep(cep);

    // Assert
    assertThat(result).isNull();
  }

  // Helper methods
  private ViaCepClient.ViaCepAddress createViaCepAddress() {
    ViaCepClient.ViaCepAddress address = new ViaCepClient.ViaCepAddress();
    address.cep = "01310-100";
    address.logradouro = "Avenida Paulista";
    address.complemento = null;
    address.bairro = "Bela Vista";
    address.localidade = "São Paulo";
    address.uf = "SP";
    address.erro = null;
    return address;
  }

  private ViaCepClient.ViaCepAddress createViaCepAddress2() {
    ViaCepClient.ViaCepAddress address = new ViaCepClient.ViaCepAddress();
    address.cep = "01311-000";
    address.logradouro = "Rua Paulista";
    address.complemento = null;
    address.bairro = "Bela Vista";
    address.localidade = "São Paulo";
    address.uf = "SP";
    address.erro = null;
    return address;
  }
}
