package com.sahaya.setu.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Set the ID card to expire after 24 hours (in milliseconds)
    private final long EXPIRATION_TIME = 86400000;

    public String generateToken(String identifier) {
        return Jwts.builder()
                .setSubject(identifier) // The user's mobile number
                .setIssuedAt(new Date()) // Time it was created
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Time it dies
                .signWith(key) // Digitally sign it so hackers cannot forge it
                .compact();
    }
}
