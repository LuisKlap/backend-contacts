package com.uex.contacts.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uex.contacts.config.AddressLookupProperties;
import com.uex.contacts.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoogleGeocodingClient {

  private final AddressLookupProperties props;
  private final RestTemplate restTemplate;

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Geometry {
    @JsonProperty("location")
    public Location location;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Location {
    public Double lat;
    public Double lng;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Result {
    public Geometry geometry;
    @JsonProperty("formatted_address")
    public String formattedAddress;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class GeocodingResponse {
    public String status;
    public Result[] results;
  }

  public Optional<Location> geocode(String query) {
    if (props.getGoogleApiKey() == null || props.getGoogleApiKey().isBlank() || query == null || query.isBlank()) {
      return Optional.empty();
    }

    try {
      // Use fromUriString para evitar problemas com versões ou caracteres
      URI uri = UriComponentsBuilder
          .fromUriString(props.getGoogleGeocodingUrl())
          .queryParam("address", query)
          .queryParam("key", props.getGoogleApiKey())
          .build()
          .encode()
          .toUri();

      ResponseEntity<GeocodingResponse> resp = restTemplate.getForEntity(uri, GeocodingResponse.class);
      GeocodingResponse body = resp.getBody();

      if (body == null) {
        return Optional.empty();
      }

      switch (body.status) {
        case "OK":
          if (body.results != null && body.results.length > 0 && body.results[0].geometry != null) {
            Location loc = body.results[0].geometry.location;
            return Optional.ofNullable(loc);
          } else {
            return Optional.empty();
          }
        case "ZERO_RESULTS":
          return Optional.empty();
        default:
          throw new ExternalServiceException("Google Geocoding returned status: " + body.status);
      }
    } catch (ExternalServiceException e) {
      throw e;
    } catch (Exception e) {
      throw new ExternalServiceException("Google Geocoding lookup failed", e);
    }
  }
}
