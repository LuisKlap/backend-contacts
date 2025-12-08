package com.uex.contacts.controller;

import com.uex.contacts.dto.address.AddressResponse;
import com.uex.contacts.service.AddressLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para intermediar consultas ao ViaCEP e Google Geocoding.
 * 
 * Seguindo o escopo do teste:
 * - Frontend não pode acessar ViaCEP diretamente
 * - Backend fornece endpoint para consultar CEP
 * - Backend fornece endpoint para buscar endereços por UF/cidade/rua
 * - Backend fornece endpoint para obter coordenadas geográficas
 */
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Tag(name = "Address Lookup", description = "Endpoints para busca de endereços via ViaCEP e geocoding")
public class AddressLookupController {

  private final AddressLookupService addressLookupService;

  @GetMapping("/cep/{cep}")
  @Operation(summary = "Busca endereço completo por CEP (via ViaCEP)")
  public ResponseEntity<AddressResponse> findByCep(
      @Parameter(description = "CEP no formato 12345678 ou 12345-678", required = true) @PathVariable String cep) {
    AddressResponse result = addressLookupService.findByCep(cep);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/search")
  @Operation(summary = "Busca endereços por UF + Cidade + Logradouro (via ViaCEP)")
  public ResponseEntity<List<AddressResponse>> search(
      @Parameter(description = "UF do estado", required = true) @RequestParam String uf,

      @Parameter(description = "Nome da cidade", required = true) @RequestParam String city,

      @Parameter(description = "Nome do logradouro (mínimo 3 caracteres)", required = true) @RequestParam String street) {
    List<AddressResponse> results = addressLookupService.searchByUfCityStreet(uf, city, street);
    return ResponseEntity.ok(results);
  }

  @GetMapping("/geocode")
  @Operation(summary = "Obtém latitude e longitude de um endereço (via Google Geocoding)")
  public ResponseEntity<AddressResponse> geocode(
      @Parameter(description = "Nome do logradouro", required = true) @RequestParam String street,

      @Parameter(description = "Número do endereço", required = false) @RequestParam(required = false) String number,

      @Parameter(description = "Nome da cidade", required = true) @RequestParam String city,

      @Parameter(description = "UF do estado", required = true) @RequestParam String state,

      @Parameter(description = "CEP", required = false) @RequestParam(required = false) String cep) {
    AddressResponse result = addressLookupService.geocodeAddress(street, number, city, state, cep);
    return ResponseEntity.ok(result);
  }
}