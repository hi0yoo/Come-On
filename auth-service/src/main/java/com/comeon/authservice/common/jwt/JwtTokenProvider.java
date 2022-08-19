package com.comeon.authservice.common.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.JSONObjectUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final String ISSUER = "come-on";

    private final String jwtSecretKey;
    private final long accessTokenExpirySec;
    private final long refreshTokenExpirySec;
    private final long reissueRefreshTokenCriteriaSec;
    private final ObjectMapper objectMapper;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecretKey,
                            @Value("${jwt.access-token.expire-time}") long accessTokenExpirySec,
                            @Value("${jwt.refresh-token.expire-time}") long refreshTokenExpirySec,
                            @Value("${jwt.refresh-token.reissue-criteria}") long reissueRefreshTokenCriteriaSec,
                            ObjectMapper objectMapper) {
        this.jwtSecretKey = jwtSecretKey;
        this.accessTokenExpirySec = accessTokenExpirySec;
        this.refreshTokenExpirySec = refreshTokenExpirySec;
        this.reissueRefreshTokenCriteriaSec = reissueRefreshTokenCriteriaSec;
        this.objectMapper = objectMapper;
    }

    public boolean validate(String token) {
        return getClaims(token) != null;
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // accessToken 생성
    public String createAccessToken(String userId, Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return buildAccessToken(userId, authorities);
    }

    // accessToken 생성
    public String createAccessToken(String userId, String role) {
        String authorities = Collections.singletonList(new SimpleGrantedAuthority(role)).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return buildAccessToken(userId, authorities);
    }

    private String buildAccessToken(String userId, String authorities) {
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

    // accessToken 재발급
    public String reissueAccessToken(String oldAccessToken) {
        String payload = new String(Base64.getDecoder().decode(oldAccessToken.split("\\.")[1]));
        Map<String, Object> objectMap = null;
        try {
            objectMap = JSONObjectUtils.parse(payload);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String userId = objectMap.get("sub").toString();
        String authorities = objectMap.get("auth").toString();

        return buildAccessToken(userId, authorities);
    }

    // refreshToken 재발급
    public Optional<String> reissueRefreshToken(String oldRefreshToken) {
        long remainSecs = Duration.between(Instant.now(), getClaims(oldRefreshToken).getExpiration().toInstant()).toSeconds();
        if (remainSecs < reissueRefreshTokenCriteriaSec) {
            return Optional.of(createRefreshToken());
        }
        return Optional.empty();
    }

    public String getUserId(String accessToken) {
        Base64.Decoder urlDecoder = Base64.getUrlDecoder();
        String payload = new String(urlDecoder.decode(accessToken.split("\\.")[1]));
        Map<String, Object> payloadObject = null;
        try {
            payloadObject = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return (String) payloadObject.get("sub");
    }
}
