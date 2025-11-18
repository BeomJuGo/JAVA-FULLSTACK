package com.healthcare.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class JwtUtil {
    private final SecretKey key;
    private final long expMin;

    public JwtUtil(String secret, long expMin) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expMin = expMin;
    }

    public String generateToken(String username, Long accId, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expMin * 60);
        return Jwts.builder()
                .setSubject(username)
                .claim("accId", accId)
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
