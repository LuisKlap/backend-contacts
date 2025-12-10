package com.dev.contacts.dto.contact;

import java.time.OffsetDateTime;

public record ContactResponse(
    Long id,
    String name,
    String cpf,
    String phone,
    String cep,
    String state,
    String city,
    String street,
    String number,
    String complement,
    String neighborhood,
    Double latitude,
    Double longitude,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {
}
