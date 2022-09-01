package com.comeon.meetingservice.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@AutoConfigureRestDocs
@MockBean(JpaMetamodelMappingContext.class)
public class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String errorCodeLink = "link:common/error-codes.html[예외 코드 참고,role=\"popup\"]";

    protected String createJson(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }

    protected String createToken(Long userId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(3600);

        String secret = "secretKeyValueForMeetingControllerTestCaseSecretKeyValueForMeetingControllerTestCase";
        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", "ROLE_USER")
                .setIssuer("come-on")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .compact();
        return "Bearer " + token;
    }

}
