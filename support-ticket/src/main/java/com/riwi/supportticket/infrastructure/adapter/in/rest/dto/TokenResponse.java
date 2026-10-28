package com.riwi.supportticket.infrastructure.adapter.in.rest.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
