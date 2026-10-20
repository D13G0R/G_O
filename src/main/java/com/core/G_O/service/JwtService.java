package com.core.G_O.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    // Usa una clave secreta larga de al menos 32 caracteres
    private static final String SECRET_KEY = "MI+CLAVE+SECRETA+SUPER+SEGURA+PARA+EL+SISTEMA+SAAS+MULTINEGOCIO";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken(Long id, String userName, String email, Long businessId) {

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("user_id", id);
        extraClaims.put("user_name", userName);
        extraClaims.put("email", email);
        extraClaims.put("business_id", businessId);


        return Jwts.builder()
                .claims(extraClaims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hora
                .signWith(getSignInKey())
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
                .verifyWith((SecretKey) getSignInKey()) // Se usa verifyWith en lugar de setSigningKey
                .build()
                .parseSignedClaims(token)              // Se usa parseSignedClaims en lugar de parseClaimsJws
                .getPayload();                         // Se usa getPayload() en lugar de getBody()
        return claimsResolver.apply(claims);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getBusinessId(String token) {
        Number businessId = extractClaim(token, claims -> claims.get("business_id", Number.class));
        return businessId == null ? null : businessId.longValue();
    }
}
/*Base64 es:
A-Z
a-z
0-9
+
/
=
*/