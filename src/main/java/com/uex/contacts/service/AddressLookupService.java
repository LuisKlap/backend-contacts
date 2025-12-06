package com.uex.contacts.service;

import com.uex.contacts.integration.GoogleGeocodingClient;
import com.uex.contacts.integration.ViaCepClient;
import com.uex.contacts.dto.address.AddressResponse;
import com.uex.contacts.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressLookupService {

  private final ViaCepClient viaCepClient;
  private final GoogleGeocodingClient googleGeocodingClient;

  @Cacheable(value = "address-lookup", key = "T(java.util.Objects).hash(#uf, #city, #query)")
  public List<AddressResponse> search(String uf, String city, String query, int limit) {
    uf = sanitizeUf(uf);
    city = sanitize(city);
    query = sanitize(query);

    final String searchQuery = query;

    if (!isValidForSearch(uf, city, searchQuery)) {
      return List.of();
    }

    try {
      List<ViaCepClient.ViaCepAddress> viaCepResults = viaCepClient.searchByUfCityStreet(uf, city, searchQuery);

      List<AddressResponse> mapped = viaCepResults.stream()
          .map(v -> AddressResponse.builder()
              .cep(v.cep)
              .logradouro(v.logradouro)
              .complemento(v.complemento)
              .bairro(v.bairro)
              .localidade(v.localidade)
              .uf(v.uf)
              .source("viacep")
              .build())
          .collect(Collectors.toList());

      if (mapped.isEmpty() && hasEnoughForGoogle(uf, city, searchQuery)) {
        Optional<GoogleGeocodingClient.Location> loc = googleGeocodingClient
            .geocode(buildGoogleQuery(uf, city, searchQuery));
        if (loc.isPresent()) {
          AddressResponse r = AddressResponse.builder()
              .cep(null)
              .logradouro(searchQuery)
              .complemento(null)
              .bairro(null)
              .localidade(city)
              .uf(uf)
              .latitude(loc.get().lat)
              .longitude(loc.get().lng)
              .source("google")
              .build();
          mapped = List.of(r);
        }
      } else {
        mapped = mapped.stream()
            .map(ar -> {
              if (ar.getLatitude() == null || ar.getLongitude() == null) {
                try {
                  String full = buildGoogleQuery(ar.getUf(), ar.getLocalidade(), ar.getLogradouro());
                  Optional<GoogleGeocodingClient.Location> loc = googleGeocodingClient.geocode(full);
                  loc.ifPresent(l -> {
                    ar.setLatitude(l.lat);
                    ar.setLongitude(l.lng);
                    ar.setSource(ar.getSource() + "+google");
                  });
                } catch (ExternalServiceException ex) {
                }
              }
              return ar;
            })
            .collect(Collectors.toList());
      }

      List<AddressResponse> ordered = mapped.stream()
          .sorted(Comparator.comparing((AddressResponse a) -> exactMatchScore(a, searchQuery)).reversed()
              .thenComparing(AddressResponse::getLocalidade)
              .thenComparing(AddressResponse::getBairro, Comparator.nullsLast(String::compareToIgnoreCase)))
          .limit(Math.max(1, limit))
          .collect(Collectors.toList());

      return ordered;
    } catch (ExternalServiceException e) {
      return List.of();
    }
  }

  private int exactMatchScore(AddressResponse a, String query) {
    if (a.getLogradouro() == null || query == null)
      return 0;
    String l = a.getLogradouro().toLowerCase();
    String q = query.toLowerCase();
    if (l.equals(q))
      return 100;
    if (l.startsWith(q))
      return 75;
    if (l.contains(q))
      return 50;
    return 0;
  }

  private boolean isValidForSearch(String uf, String city, String query) {
    return StringUtils.hasLength(uf) && uf.length() == 2 && StringUtils.hasLength(city) && StringUtils.hasLength(query)
        && query.length() >= 3;
  }

  private boolean hasEnoughForGoogle(String uf, String city, String query) {
    return StringUtils.hasLength(query) && query.length() >= 3 && StringUtils.hasLength(city);
  }

  private String sanitize(String s) {
    return s == null ? null : s.trim();
  }

  private String sanitizeUf(String uf) {
    if (uf == null)
      return null;
    return uf.trim().toUpperCase();
  }

  private String buildGoogleQuery(String uf, String city, String street) {
    StringBuilder sb = new StringBuilder();
    if (street != null)
      sb.append(street);
    if (city != null)
      sb.append(", ").append(city);
    if (uf != null)
      sb.append(", ").append(uf).append(", Brasil");
    return sb.toString();
  }
}
