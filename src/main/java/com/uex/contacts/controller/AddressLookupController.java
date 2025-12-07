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

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Tag(name = "Address Lookup", description = "Endpoints para busca de endereços")
public class AddressLookupController {

  private final AddressLookupService addressLookupService;

  @GetMapping("/lookup")
  @Operation(summary = "Busca endereços por CEP, logradouro ou texto genérico")
  public ResponseEntity<List<AddressResponse>> lookup(
      @Parameter(description = "Termo de busca: CEP, nome de rua ou texto genérico", required = true) @RequestParam String q,

      @Parameter(description = "UF (opcional, ajuda a refinar buscas por rua)", required = false) @RequestParam(required = false) String uf,

      @Parameter(description = "Cidade (opcional, ajuda a refinar buscas por rua)", required = false) @RequestParam(required = false) String city,

      @Parameter(description = "Número máximo de sugestões (padrão: 5)", required = false) @RequestParam(required = false, defaultValue = "5") Integer limit) {
    List<AddressResponse> results = addressLookupService.search(q, uf, city, limit);
    return ResponseEntity.ok(results);
  }
}