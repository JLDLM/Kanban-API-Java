package com.example.kambaapijava.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long access, refresh;

    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-ms}") long access, @Value("${app.jwt.refresh-ms}") long refresh) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.access = access;
        this.refresh = refresh;
    }

    public String create(String username, String role, boolean isRefresh) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(username).claim("role", role).claim("typ", isRefresh ? "refresh" : "access").issuedAt(new Date(now)).expiration(new Date(now + (isRefresh ? refresh : access))).signWith(key).compact();
    }

    public Claims claims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
