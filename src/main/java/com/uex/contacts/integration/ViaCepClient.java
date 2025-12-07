package com.uex.contacts.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uex.contacts.exception.ExternalServiceException;
import com.uex.contacts.config.AddressLookupProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ViaCepClient {
  private final AddressLookupProperties props;
  private final RestTemplate restTemplate;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ViaCepAddress {
    public String cep;
    @JsonProperty("logradouro")
    public String logradouro;
    @JsonProperty("complemento")
    public String complemento;
    @JsonProperty("bairro")
    public String bairro;
    @JsonProperty("localidade")
    public String localidade;
    @JsonProperty("uf")
    public String uf;
    @JsonProperty("erro")
    public Boolean erro; // ViaCep retorna { "erro": true } quando CEP não existe
  }

  /**
   * Busca direta por CEP (ex: /ws/01001000/json/)
   * 
   * @param cep CEP sem hífen (8 dígitos)
   * @return endereço ou null se não encontrado
   */
  public ViaCepAddress findByCep(String cep) {
    try {
      String url = String.format("%s/%s/json/", props.getViacepBaseUrl(), cep);
      ResponseEntity<ViaCepAddress> resp = restTemplate.getForEntity(url, ViaCepAddress.class);
      return resp.getBody();
    } catch (Exception e) {
      throw new ExternalServiceException("ViaCep CEP lookup failed", e);
    }
  }

  public List<ViaCepAddress> searchByUfCityStreet(String uf, String city, String street) {
    try {
      String url = String.format("%s/%s/%s/%s/json/",
          props.getViacepBaseUrl(),
          encodeSegment(uf),
          encodeSegment(city),
          encodeSegment(street));
      ResponseEntity<ViaCepAddress[]> resp = restTemplate.getForEntity(url, ViaCepAddress[].class);
      ViaCepAddress[] body = resp.getBody();
      if (body == null)
        return List.of();
      return Arrays.asList(body);
    } catch (Exception e) {
      throw new ExternalServiceException("ViaCep lookup failed", e);
    }
  }

  private String encodeSegment(String s) {
    return s == null ? "" : s.replace(" ", "%20");
  }
}
