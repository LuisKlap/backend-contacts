package com.uex.contacts.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "address.lookup.viacep-base-url=https://viacep.test.com.br/ws",
    "address.lookup.google-geocoding-url=https://maps.test.com/api/geocode/json",
    "address.lookup.google-api-key=test-api-key-123"
})
class AddressLookupPropertiesTest {

  @Test
  void shouldLoadPropertiesCorrectly() {
    AddressLookupProperties properties = new AddressLookupProperties();
    properties.setViacepBaseUrl("https://viacep.test.com.br/ws");
    properties.setGoogleGeocodingUrl("https://maps.test.com/api/geocode/json");
    properties.setGoogleApiKey("test-api-key-123");

    assertThat(properties.getViacepBaseUrl()).isEqualTo("https://viacep.test.com.br/ws");
    assertThat(properties.getGoogleGeocodingUrl()).isEqualTo("https://maps.test.com/api/geocode/json");
    assertThat(properties.getGoogleApiKey()).isEqualTo("test-api-key-123");
  }

  @Test
  void shouldSetViacepBaseUrl() {
    AddressLookupProperties properties = new AddressLookupProperties();
    properties.setViacepBaseUrl("https://new-viacep.com/ws");
    assertThat(properties.getViacepBaseUrl()).isEqualTo("https://new-viacep.com/ws");
  }

  @Test
  void shouldSetGoogleGeocodingUrl() {
    AddressLookupProperties properties = new AddressLookupProperties();
    properties.setGoogleGeocodingUrl("https://new-google.com/api");
    assertThat(properties.getGoogleGeocodingUrl()).isEqualTo("https://new-google.com/api");
  }

  @Test
  void shouldSetGoogleApiKey() {
    AddressLookupProperties properties = new AddressLookupProperties();
    properties.setGoogleApiKey("new-api-key");
    assertThat(properties.getGoogleApiKey()).isEqualTo("new-api-key");
  }

  @Test
  void shouldHaveDefaultValues() {
    AddressLookupProperties properties = new AddressLookupProperties();
    assertThat(properties.getViacepBaseUrl()).isEqualTo("https://viacep.com.br/ws");
    assertThat(properties.getGoogleGeocodingUrl()).isEqualTo("https://maps.googleapis.com/maps/api/geocode/json");
  }
}
