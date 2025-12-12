package com.dev.contacts.service;

import com.dev.contacts.integration.GoogleGeocodingClient;
import com.dev.contacts.integration.ViaCepClient;
import com.dev.contacts.dto.address.AddressResponse;
import com.dev.contacts.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Serviço de busca de endereços via ViaCEP e geocodificação via Google Maps.
 * 
 * Seguindo o escopo do teste:
 * - Backend intermediar consultas ao ViaCEP (frontend não pode acessar
 * diretamente)
 * - Backend usar Google Maps para obter latitude/longitude ao cadastrar
 * contatos
 * - Sistema de sugestão de endereços deve ser implementado no frontend
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressLookupService {

  private final ViaCepClient viaCepClient;
  private final GoogleGeocodingClient googleGeocodingClient;

  private static final Pattern CEP_PATTERN = Pattern.compile("^\\d{5}-?\\d{3}$");

  /**
   * Busca endereço por CEP usando ViaCEP.
   * Retorna endereço completo sem coordenadas (geocoding é feito apenas no
   * cadastro).
   */
  @Cacheable(value = "address-by-cep", key = "#cep")
  public AddressResponse findByCep(String cep) {
    String cleanCep = sanitize(cep);

    if (!isCep(cleanCep)) {
      throw new ExternalServiceException("CEP inválido");
    }

    log.debug("Searching address by CEP: {}", cleanCep);

    try {
      cleanCep = cleanCep.replaceAll("-", "");
      ViaCepClient.ViaCepAddress result = viaCepClient.findByCep(cleanCep);

      if (result == null || result.erro != null) {
        throw new ExternalServiceException("CEP não encontrado");
      }

      return mapViaCepToResponse(result);

    } catch (ExternalServiceException e) {
      log.error("Error searching CEP {}: {}", cleanCep, e.getMessage());
      throw e;
    }
  }

  /**
   * Busca endereços por UF + Cidade + Logradouro usando ViaCEP.
   * Usado para ajudar o usuário quando não sabe o CEP completo.
   */
  @Cacheable(value = "address-search", key = "#uf + '-' + #city + '-' + #street")
  public List<AddressResponse> searchByUfCityStreet(String uf, String city, String street) {
    uf = sanitizeUf(uf);
    city = sanitize(city);
    street = sanitize(street);

    if (!StringUtils.hasLength(uf) || !StringUtils.hasLength(city) || !StringUtils.hasLength(street)) {
      throw new ExternalServiceException("UF, cidade e logradouro são obrigatórios");
    }

    if (street.length() < 3) {
      throw new ExternalServiceException("Logradouro deve ter no mínimo 3 caracteres");
    }

    log.debug("Searching addresses: uf={}, city={}, street={}", uf, city, street);

    try {
      List<ViaCepClient.ViaCepAddress> results = viaCepClient.searchByUfCityStreet(uf, city, street);

      if (results.isEmpty()) {
        return List.of();
      }

      return results.stream()
          .map(this::mapViaCepToResponse)
          .limit(10) // Limita para não sobrecarregar
          .collect(Collectors.toList());

    } catch (ExternalServiceException e) {
      log.error("Error searching addresses: {}", e.getMessage());
      throw e;
    }
  }

  /**
   * Obtém latitude e longitude de um endereço usando Google Geocoding API.
   * Usado ao cadastrar/atualizar contatos.
   */
  public AddressResponse geocodeAddress(String street, String number, String city, String state, String cep) {
    String fullAddress = buildGoogleQuery(street, number, city, state, cep);

    log.debug("Geocoding address: {}", fullAddress);

    try {
      Optional<GoogleGeocodingClient.Location> location = googleGeocodingClient.geocode(fullAddress);

      if (location.isEmpty()) {
        throw new ExternalServiceException("Não foi possível obter as coordenadas do endereço");
      }

      GoogleGeocodingClient.Location loc = location.get();

      return AddressResponse.builder()
          .cep(cep)
          .logradouro(street)
          .localidade(city)
          .uf(state)
          .latitude(loc.lat)
          .longitude(loc.lng)
          .source("google-geocoding")
          .exact(true)
          .build();

    } catch (ExternalServiceException e) {
      log.error("Error geocoding address {}: {}", fullAddress, e.getMessage());
      throw e;
    }
  }

  private AddressResponse mapViaCepToResponse(ViaCepClient.ViaCepAddress v) {
    return AddressResponse.builder()
        .cep(formatCep(v.cep))
        .logradouro(v.logradouro)
        .complemento(v.complemento)
        .bairro(v.bairro)
        .localidade(v.localidade)
        .uf(v.uf)
        .source("viacep")
        .exact(true)
        .build();
  }

  private boolean isCep(String text) {
    return text != null && CEP_PATTERN.matcher(text).matches();
  }

  private String sanitize(String s) {
    return s == null ? null : s.trim();
  }

  private String sanitizeUf(String uf) {
    if (uf == null)
      return null;
    return uf.trim().toUpperCase();
  }

  private String buildGoogleQuery(String street, String number, String city, String state, String cep) {
    StringBuilder sb = new StringBuilder();

    if (StringUtils.hasLength(street)) {
      sb.append(street);
    }
    if (StringUtils.hasLength(number)) {
      sb.append(", ").append(number);
    }
    if (StringUtils.hasLength(city)) {
      if (sb.length() > 0)
        sb.append(", ");
      sb.append(city);
    }
    if (StringUtils.hasLength(state)) {
      if (sb.length() > 0)
        sb.append(", ");
      sb.append(state);
    }
    if (StringUtils.hasLength(cep)) {
      if (sb.length() > 0)
        sb.append(", ");
      sb.append(cep);
    }

    sb.append(", Brasil");
    return sb.toString();
  }

  private String formatCep(String cep) {
    if (cep == null || cep.length() != 8)
      return cep;
    return cep.substring(0, 5) + "-" + cep.substring(5);
  }

}
