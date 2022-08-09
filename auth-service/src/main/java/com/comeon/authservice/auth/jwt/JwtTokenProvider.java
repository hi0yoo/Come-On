package com.comeon.authservice.auth.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final String ISSUER = "come-on";

    private final String jwtSecretKey;
    private final long accessTokenExpirySec;
    private final long refreshTokenExpirySec;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecretKey,
                            @Value("${jwt.access-token.expire-time}") long accessTokenExpirySec,
                            @Value("${jwt.refresh-token.expire-time}") long refreshTokenExpirySec) {
        this.jwtSecretKey = jwtSecretKey;
        this.accessTokenExpirySec = accessTokenExpirySec;
        this.refreshTokenExpirySec = refreshTokenExpirySec;
    }

    // accessToken 생성
    public String createAccessToken(String userId, Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(accessTokenExpirySec);

        return Jwts.builder()
                .setSubject(userId)
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer(ISSUER)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .compact();
    }

    // refreshToken 생성
    public String createRefreshToken() {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(refreshTokenExpirySec);

        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setIssuer(ISSUER)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .compact();
    }
}
