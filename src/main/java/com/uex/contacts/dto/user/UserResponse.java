package com.uex.contacts.dto.user;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        OffsetDateTime createdAt) {
}
