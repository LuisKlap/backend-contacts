package com.uex.contacts.service;

import com.uex.contacts.integration.GoogleGeocodingClient;
import com.uex.contacts.integration.ViaCepClient;
import com.uex.contacts.dto.address.AddressResponse;
import com.uex.contacts.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressLookupService {

  private final ViaCepClient viaCepClient;
  private final GoogleGeocodingClient googleGeocodingClient;

  private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");

  @Cacheable(value = "address-lookup", key = "T(java.util.Objects).hash(#q, #uf, #city, #limit)")
  public List<AddressResponse> search(String q, String uf, String city, int limit) {
    q = sanitize(q);
    uf = sanitizeUf(uf);
    city = sanitize(city);

    if (!StringUtils.hasLength(q) || q.length() < 3) {
      return List.of();
    }

    // 1. DETECTAR CEP EXATO
    if (isCep(q)) {
      return searchByCep(q, limit);
    }

    // 2. BUSCA POR UF + CIDADE + RUA (ViaCep)
    if (StringUtils.hasLength(uf) && StringUtils.hasLength(city)) {
      return searchByUfCityStreet(uf, city, q, limit);
    }

    // 3. FALLBACK: BUSCA GENÉRICA COM GOOGLE
    return searchWithGoogle(q, city, uf, limit);
  }

  /**
   * 1. Busca direta por CEP no ViaCep + enriquecimento com Google
   */
  private List<AddressResponse> searchByCep(String cep, int limit) {
    try {
      String cleanCep = cep.replaceAll("-", "");
      ViaCepClient.ViaCepAddress result = viaCepClient.findByCep(cleanCep);

      if (result == null || result.erro != null) {
        return List.of();
      }

      AddressResponse response = mapViaCepToResponse(result, true);

      // Enriquecer com lat/lng do Google
      enrichWithGoogleCoordinates(response);

      return List.of(response);

    } catch (ExternalServiceException e) {
      return List.of();
    }
  }

  /**
   * 2. Busca por UF + Cidade + Logradouro (ViaCep search)
   */
  private List<AddressResponse> searchByUfCityStreet(String uf, String city, String street, int limit) {
    try {
      List<ViaCepClient.ViaCepAddress> viaCepResults = viaCepClient.searchByUfCityStreet(uf, city, street);

      List<AddressResponse> responses = viaCepResults.stream()
          .map(v -> mapViaCepToResponse(v, false))
          .limit(Math.max(1, limit))
          .collect(Collectors.toList());

      // Geocodificar APENAS o primeiro resultado (economia)
      if (!responses.isEmpty()) {
        enrichWithGoogleCoordinates(responses.get(0));
      }

      return orderByRelevance(responses, street, limit);

    } catch (ExternalServiceException e) {
      return searchWithGoogle(street, city, uf, limit);
    }
  }

  /**
   * 3. Busca genérica com Google Geocoding (fallback)
   */
  private List<AddressResponse> searchWithGoogle(String query, String city, String uf, int limit) {
    try {
      String googleQuery = buildGoogleQuery(uf, city, query);
      Optional<GoogleGeocodingClient.Location> loc = googleGeocodingClient.geocode(googleQuery);

      if (loc.isPresent()) {
        AddressResponse response = AddressResponse.builder()
            .cep(null)
            .logradouro(query)
            .complemento(null)
            .bairro(null)
            .localidade(city)
            .uf(uf)
            .latitude(loc.get().lat)
            .longitude(loc.get().lng)
            .source("google")
            .exact(false)
            .build();

        response.setDisplay(buildDisplayText(response));
        return List.of(response);
      }

      return List.of();

    } catch (ExternalServiceException e) {
      return List.of();
    }
  }

  private AddressResponse mapViaCepToResponse(ViaCepClient.ViaCepAddress v, boolean exact) {
    AddressResponse response = AddressResponse.builder()
        .cep(v.cep)
        .logradouro(v.logradouro)
        .complemento(v.complemento)
        .bairro(v.bairro)
        .localidade(v.localidade)
        .uf(v.uf)
        .source("viacep")
        .exact(exact)
        .build();

    response.setDisplay(buildDisplayText(response));
    return response;
  }

  private void enrichWithGoogleCoordinates(AddressResponse response) {
    if (response.getLatitude() != null)
      return;

    try {
      String fullAddress = buildGoogleQuery(response.getUf(), response.getLocalidade(), response.getLogradouro());
      Optional<GoogleGeocodingClient.Location> loc = googleGeocodingClient.geocode(fullAddress);

      loc.ifPresent(l -> {
        response.setLatitude(l.lat);
        response.setLongitude(l.lng);
        response.setSource("viacep+google");
      });
    } catch (ExternalServiceException ex) {
      // Silently fail - coordinates are optional
    }
  }

  private List<AddressResponse> orderByRelevance(List<AddressResponse> responses, String query, int limit) {
    return responses.stream()
        .sorted(Comparator
            .comparing((AddressResponse a) -> exactMatchScore(a, query)).reversed()
            .thenComparing(AddressResponse::getLocalidade)
            .thenComparing(AddressResponse::getBairro, Comparator.nullsLast(String::compareToIgnoreCase)))
        .limit(Math.max(1, limit))
        .collect(Collectors.toList());
  }

  private boolean isCep(String text) {
    return text != null && CEP_PATTERN.matcher(text).matches();
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

  /**
   * Formata endereço para exibição:
   * "Rua das Flores - Centro, Curitiba - PR, 80000-000"
   */
  private String buildDisplayText(AddressResponse addr) {
    StringBuilder sb = new StringBuilder();

    if (StringUtils.hasLength(addr.getLogradouro())) {
      sb.append(addr.getLogradouro());
    }

    if (StringUtils.hasLength(addr.getBairro())) {
      if (sb.length() > 0)
        sb.append(" - ");
      sb.append(addr.getBairro());
    }

    if (StringUtils.hasLength(addr.getLocalidade())) {
      if (sb.length() > 0)
        sb.append(", ");
      sb.append(addr.getLocalidade());
    }

    if (StringUtils.hasLength(addr.getUf())) {
      if (sb.length() > 0)
        sb.append(" - ");
      sb.append(addr.getUf());
    }

    if (StringUtils.hasLength(addr.getCep())) {
      if (sb.length() > 0)
        sb.append(", ");
      sb.append(formatCep(addr.getCep()));
    }

    return sb.toString();
  }

  private String formatCep(String cep) {
    if (cep == null || cep.length() != 8)
      return cep;
    return cep.substring(0, 5) + "-" + cep.substring(5);
  }
}
