package com.uex.contacts.controller;

import com.uex.contacts.dto.address.AddressResponse;
import com.uex.contacts.service.AddressLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address-lookup")
@RequiredArgsConstructor
public class AddressLookupController {

  private final AddressLookupService addressLookupService;

  @GetMapping
  public ResponseEntity<List<AddressResponse>> search(
      @RequestParam String uf,
      @RequestParam String city,
      @RequestParam(name = "q") String query,
      @RequestParam(name = "limit", defaultValue = "10") int limit) {
    List<AddressResponse> list = addressLookupService.search(uf, city, query, limit);
    return ResponseEntity.ok(list);
  }
}
