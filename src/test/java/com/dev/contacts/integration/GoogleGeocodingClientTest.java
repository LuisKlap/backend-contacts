package com.dev.contacts.integration;

import com.dev.contacts.config.AddressLookupProperties;
import com.dev.contacts.exception.ExternalServiceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleGeocodingClient Tests")
class GoogleGeocodingClientTest {

  @Mock
  private AddressLookupProperties properties;

  @Mock
  private RestTemplate restTemplate;

  private GoogleGeocodingClient googleGeocodingClient;

  @BeforeEach
  void setUp() {
    googleGeocodingClient = new GoogleGeocodingClient(properties, restTemplate);
  }

  @Test
  @DisplayName("Deve geocodificar endereço com sucesso")
  void shouldGeocodeAddressSuccessfully() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Avenida Paulista, 1000, São Paulo, SP";
    GoogleGeocodingClient.GeocodingResponse response = createSuccessResponse(-23.561684, -46.655981);

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().lat).isEqualTo(-23.561684);
    assertThat(result.get().lng).isEqualTo(-46.655981);
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando API key não está configurada")
  void shouldReturnEmptyWhenApiKeyNotConfigured() {
    // Arrange
    when(properties.getGoogleApiKey()).thenReturn(null);
    googleGeocodingClient = new GoogleGeocodingClient(properties, restTemplate);

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode("Some address");

    // Assert
    assertThat(result).isEmpty();
    verify(restTemplate, never()).getForEntity(any(URI.class), any());
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando API key está vazia")
  void shouldReturnEmptyWhenApiKeyIsBlank() {
    // Arrange
    when(properties.getGoogleApiKey()).thenReturn("   ");
    googleGeocodingClient = new GoogleGeocodingClient(properties, restTemplate);

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode("Some address");

    // Assert
    assertThat(result).isEmpty();
    verify(restTemplate, never()).getForEntity(any(URI.class), any());
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando query é nula")
  void shouldReturnEmptyWhenQueryIsNull() {
    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(null);

    // Assert
    assertThat(result).isEmpty();
    verify(restTemplate, never()).getForEntity(any(URI.class), any());
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando query está vazia")
  void shouldReturnEmptyWhenQueryIsBlank() {
    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode("   ");

    // Assert
    assertThat(result).isEmpty();
    verify(restTemplate, never()).getForEntity(any(URI.class), any());
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando status é ZERO_RESULTS")
  void shouldReturnEmptyWhenStatusIsZeroResults() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Endereço Inexistente 999999";
    GoogleGeocodingClient.GeocodingResponse response = new GoogleGeocodingClient.GeocodingResponse();
    response.status = "ZERO_RESULTS";
    response.results = new GoogleGeocodingClient.Result[0];

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando response body é nulo")
  void shouldReturnEmptyWhenResponseBodyIsNull() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Some address";

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(null));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando results é nulo")
  void shouldReturnEmptyWhenResultsIsNull() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Some address";
    GoogleGeocodingClient.GeocodingResponse response = new GoogleGeocodingClient.GeocodingResponse();
    response.status = "OK";
    response.results = null;

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando results está vazio")
  void shouldReturnEmptyWhenResultsIsEmpty() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Some address";
    GoogleGeocodingClient.GeocodingResponse response = new GoogleGeocodingClient.GeocodingResponse();
    response.status = "OK";
    response.results = new GoogleGeocodingClient.Result[0];

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve retornar Optional vazio quando geometry é nulo")
  void shouldReturnEmptyWhenGeometryIsNull() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Some address";
    GoogleGeocodingClient.GeocodingResponse response = new GoogleGeocodingClient.GeocodingResponse();
    response.status = "OK";
    response.results = new GoogleGeocodingClient.Result[1];
    response.results[0] = new GoogleGeocodingClient.Result();
    response.results[0].geometry = null;

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Deve lançar ExternalServiceException quando status é REQUEST_DENIED")
  void shouldThrowExceptionWhenStatusIsRequestDenied() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Some address";
    GoogleGeocodingClient.GeocodingResponse response = new GoogleGeocodingClient.GeocodingResponse();
    response.status = "REQUEST_DENIED";

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act & Assert
    assertThatThrownBy(() -> googleGeocodingClient.geocode(query))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("Google Geocoding returned status: REQUEST_DENIED");
  }

  @Test
  @DisplayName("Deve lançar ExternalServiceException quando status é INVALID_REQUEST")
  void shouldThrowExceptionWhenStatusIsInvalidRequest() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Some address";
    GoogleGeocodingClient.GeocodingResponse response = new GoogleGeocodingClient.GeocodingResponse();
    response.status = "INVALID_REQUEST";

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act & Assert
    assertThatThrownBy(() -> googleGeocodingClient.geocode(query))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("Google Geocoding returned status: INVALID_REQUEST");
  }

  @Test
  @DisplayName("Deve lançar ExternalServiceException quando status é OVER_QUERY_LIMIT")
  void shouldThrowExceptionWhenStatusIsOverQueryLimit() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Some address";
    GoogleGeocodingClient.GeocodingResponse response = new GoogleGeocodingClient.GeocodingResponse();
    response.status = "OVER_QUERY_LIMIT";

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act & Assert
    assertThatThrownBy(() -> googleGeocodingClient.geocode(query))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("Google Geocoding returned status: OVER_QUERY_LIMIT");
  }

  @Test
  @DisplayName("Deve lançar ExternalServiceException quando RestTemplate falhar")
  void shouldThrowExceptionWhenRestTemplateFails() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Some address";

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenThrow(new RestClientException("Connection timeout"));

    // Act & Assert
    assertThatThrownBy(() -> googleGeocodingClient.geocode(query))
        .isInstanceOf(ExternalServiceException.class)
        .hasMessageContaining("Google Geocoding lookup failed");
  }

  @Test
  @DisplayName("Deve lançar ExternalServiceException e encapsular exceção original")
  void shouldThrowExceptionAndWrapOriginalException() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Some address";
    RestClientException originalException = new RestClientException("Network error");

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenThrow(originalException);

    // Act & Assert
    assertThatThrownBy(() -> googleGeocodingClient.geocode(query))
        .isInstanceOf(ExternalServiceException.class)
        .hasCause(originalException);
  }

  @Test
  @DisplayName("Deve geocodificar com coordenadas negativas")
  void shouldGeocodeWithNegativeCoordinates() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "São Paulo, Brazil";
    GoogleGeocodingClient.GeocodingResponse response = createSuccessResponse(-23.550520, -46.633308);

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().lat).isNegative();
    assertThat(result.get().lng).isNegative();
  }

  @Test
  @DisplayName("Deve geocodificar com coordenadas positivas")
  void shouldGeocodeWithPositiveCoordinates() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Tokyo, Japan";
    GoogleGeocodingClient.GeocodingResponse response = createSuccessResponse(35.6762, 139.6503);

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().lat).isPositive();
    assertThat(result.get().lng).isPositive();
  }

  @Test
  @DisplayName("Deve geocodificar endereço com caracteres especiais")
  void shouldGeocodeAddressWithSpecialCharacters() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Rua São João, 123 - São Paulo/SP";
    GoogleGeocodingClient.GeocodingResponse response = createSuccessResponse(-23.561684, -46.655981);

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isPresent();
  }

  @Test
  @DisplayName("Deve retornar primeiro resultado quando múltiplos resultados são retornados")
  void shouldReturnFirstResultWhenMultipleResultsReturned() {
    // Arrange
    when(properties.getGoogleGeocodingUrl()).thenReturn("https://maps.googleapis.com/maps/api/geocode/json");
    when(properties.getGoogleApiKey()).thenReturn("test-api-key");
    String query = "Main Street";
    GoogleGeocodingClient.GeocodingResponse response = new GoogleGeocodingClient.GeocodingResponse();
    response.status = "OK";
    response.results = new GoogleGeocodingClient.Result[3];

    // Primeiro resultado
    response.results[0] = new GoogleGeocodingClient.Result();
    response.results[0].geometry = new GoogleGeocodingClient.Geometry();
    response.results[0].geometry.location = new GoogleGeocodingClient.Location();
    response.results[0].geometry.location.lat = 10.0;
    response.results[0].geometry.location.lng = 20.0;

    // Segundo resultado (não deve ser retornado)
    response.results[1] = new GoogleGeocodingClient.Result();
    response.results[1].geometry = new GoogleGeocodingClient.Geometry();
    response.results[1].geometry.location = new GoogleGeocodingClient.Location();
    response.results[1].geometry.location.lat = 30.0;
    response.results[1].geometry.location.lng = 40.0;

    when(restTemplate.getForEntity(
        any(URI.class),
        eq(GoogleGeocodingClient.GeocodingResponse.class)))
        .thenReturn(ResponseEntity.ok(response));

    // Act
    Optional<GoogleGeocodingClient.Location> result = googleGeocodingClient.geocode(query);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().lat).isEqualTo(10.0);
    assertThat(result.get().lng).isEqualTo(20.0);
  }

  // Helper methods
  private GoogleGeocodingClient.GeocodingResponse createSuccessResponse(double lat, double lng) {
    GoogleGeocodingClient.GeocodingResponse response = new GoogleGeocodingClient.GeocodingResponse();
    response.status = "OK";
    response.results = new GoogleGeocodingClient.Result[1];
    response.results[0] = new GoogleGeocodingClient.Result();
    response.results[0].geometry = new GoogleGeocodingClient.Geometry();
    response.results[0].geometry.location = new GoogleGeocodingClient.Location();
    response.results[0].geometry.location.lat = lat;
    response.results[0].geometry.location.lng = lng;
    response.results[0].formattedAddress = "Test Address";
    return response;
  }
}
