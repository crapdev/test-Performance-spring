package com.riwi.supportticket.infrastructure.adapter.out.security;

import com.riwi.supportticket.application.port.out.TokenPort;
import com.riwi.supportticket.domain.model.user.Actor;
import com.riwi.supportticket.domain.model.user.Role;
import com.riwi.supportticket.domain.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

// Firma HS256. El secreto viene de JWT_SECRET (mínimo 32 caracteres = 256 bits)
@Component
public class JwtTokenProvider implements TokenPort {

    private final SecretKey key;
    private final Duration expiration;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration-minutes}") long expirationMinutes) {
        // hmacShaKeyFor falla al arrancar si el secreto es muy corto
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    @Override
    public String generate(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    // Valida firma y expiración. Devuelve null si el token no sirve.
    public Actor parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            Role role = Role.valueOf(claims.get("role", String.class));
            return new Actor(userId, role);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
