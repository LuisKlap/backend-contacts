package com.uex.contacts.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uex.contacts.config.AddressLookupProperties;
import com.uex.contacts.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoogleGeocodingClient {
  private final AddressLookupProperties props;
  private final RestTemplate restTemplate = new RestTemplate();

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
    if (props.getGoogleApiKey() == null || props.getGoogleApiKey().isBlank()) {
      return Optional.empty();
    }

    try {
      String url = String.format("%s?address=%s&key=%s",
          props.getGoogleGeocodingUrl(),
          encode(query),
          props.getGoogleApiKey());
      ResponseEntity<GeocodingResponse> resp = restTemplate.getForEntity(url, GeocodingResponse.class);
      GeocodingResponse body = resp.getBody();
      if (body == null || !"OK".equals(body.status) || body.results == null || body.results.length == 0) {
        return Optional.empty();
      }
      Location loc = body.results[0].geometry.location;
      return Optional.ofNullable(loc);
    } catch (Exception e) {
      throw new ExternalServiceException("Google Geocoding lookup failed", e);
    }
  }

  private String encode(String s) {
    return s == null ? "" : s.replace(" ", "+");
  }
}
