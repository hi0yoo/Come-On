package com.comeon.authservice.docs.api;

import com.comeon.authservice.docs.config.RestDocsSupport;
import com.comeon.authservice.domain.refreshtoken.dto.RefreshTokenDto;
import com.comeon.authservice.domain.refreshtoken.entity.RefreshToken;
import com.comeon.authservice.domain.refreshtoken.repository.RefreshTokenRepository;
import com.comeon.authservice.domain.user.entity.OAuthProvider;
import com.comeon.authservice.domain.user.entity.User;
import com.comeon.authservice.domain.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultActions;

import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class AuthServiceApi extends RestDocsSupport {

    static String TOKEN_TYPE_BEARER = "Bearer ";

    @Value("${jwt.secret}")
    String jwtSecretKey;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void initData() {
        User user = new User("12345",
                "user1@email.com",
                "user1",
                "user1",
                "user1.profileImg",
                OAuthProvider.KAKAO
        );
        user = userRepository.save(user);
    }

    @Test
    @DisplayName("Reissue Tokens Api Docs")
    void reissueTokensSuccess() throws Exception {
        User user = userRepository.findById(1L).orElseThrow();

        String authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getRoleValue())).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 현재 시간 5분 전으로 발행일 세팅
        Instant issuedAt = Instant.now().minusSeconds(300);
        // 발행일 + 2분으로 만료일자 세팅
        Instant expiryDate = issuedAt.plusSeconds(120);

        String accessToken = Jwts.builder()
                .setSubject(user.getId().toString())
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", authorities)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate))
                .compact();

        String refreshTokenValue = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .setIssuer("test")
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiryDate.plusSeconds(3000))) // refreshToken 만료일 지정. 50분 추가
                .compact();

        RefreshToken refreshToken = new RefreshToken(
                new RefreshTokenDto(
                        user,
                        refreshTokenValue
                )
        );
        refreshToken = refreshTokenRepository.save(refreshToken);

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken.getToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(300);

        ResultActions perform = mockMvc.perform(
                post("/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TOKEN_TYPE_BEARER + accessToken)
                        .cookie(refreshTokenCookie)
        );

        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.isRefreshTokenReissued").value(true));

        perform.andDo(
                restDocs.document(
                        responseFields(
                                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("Reissued Access Token"),
                                fieldWithPath("isRefreshTokenReissued").type(JsonFieldType.BOOLEAN).description("Is Refresh Token Reissued")
                        )
                )
        );
    }
}
