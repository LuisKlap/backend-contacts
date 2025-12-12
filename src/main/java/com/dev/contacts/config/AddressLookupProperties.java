package com.dev.contacts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "address.lookup")
public class AddressLookupProperties {

  private String viacepBaseUrl = "https://viacep.com.br/ws";

  private String googleGeocodingUrl = "https://maps.googleapis.com/maps/api/geocode/json";

  private String googleApiKey;

  public String getViacepBaseUrl() {
    return viacepBaseUrl;
  }

  public void setViacepBaseUrl(String viacepBaseUrl) {
    this.viacepBaseUrl = viacepBaseUrl;
  }

  public String getGoogleGeocodingUrl() {
    return googleGeocodingUrl;
  }

  public void setGoogleGeocodingUrl(String googleGeocodingUrl) {
    this.googleGeocodingUrl = googleGeocodingUrl;
  }

  public String getGoogleApiKey() {
    return googleApiKey;
  }

  public void setGoogleApiKey(String googleApiKey) {
    this.googleApiKey = googleApiKey;
  }
}
