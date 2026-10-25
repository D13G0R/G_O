package com.core.G_O.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);
    private static final int MINIMUM_KEY_BYTES = 32;

    private final SecretKey signingKey;

    public JwtService(@Value("${app.security.jwt-secret:}") String configuredSecret) {
        this.signingKey = resolveSigningKey(configuredSecret);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(Long id, String userName, String email, Long businessId, String role, Long technicianId) {

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("user_id", id);
        extraClaims.put("user_name", userName);
        extraClaims.put("email", email);
        extraClaims.put("business_id", businessId);
        extraClaims.put("role", role);
        extraClaims.put("technician_id", technicianId);


        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hora
                .signWith(signingKey)
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    public Long getBusinessId(String token) {
        Number businessId = extractClaim(token, claims -> claims.get("business_id", Number.class));
        return businessId == null ? null : businessId.longValue();
    }

    public Long getUserId(String token) {
        Number userId = extractClaim(token, claims -> claims.get("user_id", Number.class));
        return userId == null ? null : userId.longValue();
    }

    public Long getTechnicianId(String token) {
        Number technicianId = extractClaim(token, claims -> claims.get("technician_id", Number.class));
        return technicianId == null ? null : technicianId.longValue();
    }

    public String getRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    private SecretKey resolveSigningKey(String configuredSecret) {
        if (configuredSecret == null || configuredSecret.isBlank()) {
            byte[] ephemeralKey = new byte[MINIMUM_KEY_BYTES];
            new SecureRandom().nextBytes(ephemeralKey);
            LOG.warn("@JwtService > JWT_SECRET no configurado. Se generó una clave efímera; " +
                    "los tokens se invalidarán al reiniciar. Configura JWT_SECRET en producción.");
            return Keys.hmacShaKeyFor(ephemeralKey);
        }

        byte[] keyBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);

        if (configuredSecret.matches("^[A-Za-z0-9+/=]+$")) {
            try {
                byte[] decoded = Decoders.BASE64.decode(configuredSecret);
                if (decoded.length >= MINIMUM_KEY_BYTES) {
                    keyBytes = decoded;
                }
            } catch (DecodingException ignored) {
                // Si no es Base64 válido se usa el texto plano como clave.
            }
        }

        if (keyBytes.length < MINIMUM_KEY_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos " + MINIMUM_KEY_BYTES + " bytes (texto o Base64).");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
