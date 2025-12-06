package com.uex.contacts.dto.address;

import jakarta.validation.constraints.NotBlank;

public record AddressResponse(
        @NotBlank String cep,
        @NotBlank String state,
        @NotBlank String city,
        @NotBlank String street,
        String neighborhood) {
}
