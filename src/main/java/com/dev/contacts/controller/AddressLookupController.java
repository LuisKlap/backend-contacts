package com.dev.contacts.controller;

import com.dev.contacts.dto.address.AddressResponse;
import com.dev.contacts.exception.BadRequestException;
import com.dev.contacts.service.AddressLookupService;
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
      @Parameter(description = "UF do estado", required = true) @RequestParam(required = false) String uf,

      @Parameter(description = "Nome da cidade", required = true) @RequestParam(required = false) String city,

      @Parameter(description = "Nome do logradouro (mínimo 3 caracteres)", required = true) @RequestParam(required = false) String street) {
    if (uf == null || uf.isBlank()) {
      throw new BadRequestException("Parâmetro 'uf' é obrigatório");
    }
    if (city == null || city.isBlank()) {
      throw new BadRequestException("Parâmetro 'city' é obrigatório");
    }
    if (street == null || street.isBlank()) {
      throw new BadRequestException("Parâmetro 'street' é obrigatório");
    }
    List<AddressResponse> results = addressLookupService.searchByUfCityStreet(uf, city, street);
    return ResponseEntity.ok(results);
  }

  @GetMapping("/geocode")
  @Operation(summary = "Obtém latitude e longitude de um endereço (via Google Geocoding)")
  public ResponseEntity<AddressResponse> geocode(
      @Parameter(description = "Nome do logradouro", required = true) @RequestParam(required = false) String street,

      @Parameter(description = "Número do endereço", required = false) @RequestParam(required = false) String number,

      @Parameter(description = "Nome da cidade", required = true) @RequestParam(required = false) String city,

      @Parameter(description = "UF do estado", required = true) @RequestParam(required = false) String state,

      @Parameter(description = "CEP", required = false) @RequestParam(required = false) String cep) {
    if (street == null || street.isBlank()) {
      throw new BadRequestException("Parâmetro 'street' é obrigatório");
    }
    if (city == null || city.isBlank()) {
      throw new BadRequestException("Parâmetro 'city' é obrigatório");
    }
    if (state == null || state.isBlank()) {
      throw new BadRequestException("Parâmetro 'state' é obrigatório");
    }
    AddressResponse result = addressLookupService.geocodeAddress(street, number, city, state, cep);
    return ResponseEntity.ok(result);
  }
}