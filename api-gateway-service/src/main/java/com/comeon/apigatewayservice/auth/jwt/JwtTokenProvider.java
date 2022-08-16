package com.comeon.apigatewayservice.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenProvider {

    private final String jwtSecretKey;
    private final JwtRepository jwtRepository;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecretKey,
                            JwtRepository jwtRepository) {
        this.jwtSecretKey = jwtSecretKey;
        this.jwtRepository = jwtRepository;
    }

    public boolean validate(String accessToken) {
        if (jwtRepository.findAccessToken(accessToken).isEmpty()) {
            try {
                return getClaims(accessToken) != null;
            } catch (JwtException e) {
                return false;
            }
        }
        return false;
    }

    public Claims getClaims(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }
}
