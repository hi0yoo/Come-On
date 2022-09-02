package com.comeon.meetingservice.web;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.web.common.util.TokenUtils;
import com.comeon.meetingservice.web.meetinguser.query.MeetingUserQueryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.BDDMockito.given;

@AutoConfigureRestDocs
@MockBean(JpaMetamodelMappingContext.class)
@ActiveProfiles("test")
public abstract class ControllerTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String errorCodeLink = "link:common/error-codes.html[예외 코드 참고,role=\"popup\"]";

    protected String createJson(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }

    // ===== TOKENUTILS MOCKING ===== //

    MockedStatic<TokenUtils> tokenUtilsMock;

    protected String createToken(Long userId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusSeconds(3600);

        String secret = "secretKeyValueForMeetingControllerTestCaseSecretKeyValueForMeetingControllerTestCase";
        String token = "Bearer " + Jwts.builder()
                .setSubject(String.valueOf(userId))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", "ROLE_USER")
                .setIssuer("come-on")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .compact();

        // MockMvc 테스트 시 TokenUtils가 빈으로 등록되지 않아 static method 모킹함
        // ArgumentResolver, Interceptor 에서 해당 메서드를 사용하기 때문에 필요.
        given(TokenUtils.getUserId(token)).willReturn(userId);

        return token;
    }

    // static method 모킹하기 위해 작성 (테스트 끝나면 모킹 해제해야 함),
    // 항상 부모 -> 자식 순(선언 순)으로 실행 따라서 테스트 케이스는 항상 아래의 메서드로 초기화가 진행됨
    @BeforeEach
    protected void initTokenUtils() {
        tokenUtilsMock = Mockito.mockStatic(TokenUtils.class);
    }

    @AfterEach
    protected void closeTokenUtils() {
        tokenUtilsMock.close();
    }

    // ===== INTERCEPTOR MOCKING ===== //

    @MockBean
    MeetingUserQueryRepository meetingUserQueryRepository;

    // 추후 컨트롤러 테스트에서 사용하면 됨
    protected Long mockedExistentMeetingId;
    protected Long mockedNonexistentMeetingId;
    protected Long mockedHostUserId;
    protected Long mockedEditorUserId;
    protected Long mockedParticipantUserId;

    // Interceptor에서 meetingUserQueryRepository 사용 시 샘플 모임 유저 리스트를 반환하기 위해 공통적으로 모킹함
    @BeforeEach
    protected void mockingMeetingUserQueryRepository() {

        mockedHostUserId = 1000L;
        mockedEditorUserId = 2000L;
        mockedParticipantUserId = 3000L;

        List<MeetingUserEntity> meetingUserEntities = new ArrayList<>();

        MeetingUserEntity hostUser = MeetingUserEntity.builder()
                .userId(mockedHostUserId)
                .meetingRole(MeetingRole.HOST)
                .build();

        MeetingUserEntity editorUser = MeetingUserEntity.builder()
                .userId(mockedEditorUserId)
                .meetingRole(MeetingRole.PARTICIPANT)
                .build();

        MeetingUserEntity participantUser = MeetingUserEntity.builder()
                .userId(mockedParticipantUserId)
                .meetingRole(MeetingRole.PARTICIPANT)
                .build();

        meetingUserEntities.add(hostUser);
        meetingUserEntities.add(editorUser);
        meetingUserEntities.add(participantUser);

        // 존재하는 모임이라면 회원 리스트를 반환, 아니라면 빈 리스트 반환
        // 인터셉터에서 모임이 존재하는지 판단하는 데 사용, 회원이 있는지 판단하는데 사용, 회원이 HOST인지 판단하는데 사용
        mockedExistentMeetingId = 1000L;
        mockedNonexistentMeetingId = 2000L;
        given(meetingUserQueryRepository.findAllByMeetingId(mockedExistentMeetingId)).willReturn(meetingUserEntities);
        given(meetingUserQueryRepository.findAllByMeetingId(mockedNonexistentMeetingId)).willReturn(new ArrayList<>());
    }
}
