package com.comeon.meetingservice.web.common.interceptor;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.web.common.exception.AuthorizationFailException;
import com.comeon.meetingservice.web.common.util.TokenUtils;
import com.comeon.meetingservice.web.meetinguser.query.MeetingUserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class MeetingAuthInterceptor implements HandlerInterceptor {

    private final MeetingUserQueryRepository meetingUserQueryRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("[MeetingAuthInterceptor] 발동 - 요청 경로: {}", request.getRequestURI());

        if (!(handler instanceof HandlerMethod)) {
            // 요청 경로에 맞는 핸들러가 없는 경우 true로 인터셉터 통과 -> NoHandlerFoundException 발생함
            return true;
        }

        MeetingAuth meetingAuth = ((HandlerMethod) handler).getMethodAnnotation(MeetingAuth.class);
        if (Objects.isNull(meetingAuth)) {
            return true;
        }

        Long meetingId = getMeetingId(request);

        List<MeetingUserEntity> meetingUsers = meetingUserQueryRepository.findAllByMeetingId(meetingId);

        // 없는 모임 리소스인지 확인
        checkEmptyMeeting(meetingUsers);

        // 요청을 보낸 회원이 모임에 가입되었는지 확인 및 구해오기
        MeetingUserEntity requestUser = checkUserIncludeAndGet(request, meetingUsers);


        // 회원의 권한 체크, 회원의 역할이 requiringRole 중에 하나라도 포함된다면 성공
        MeetingRole[] requiringRoles = meetingAuth.meetingRoles();

        Arrays.stream(requiringRoles)
                .filter((requiringRole) -> authCheck(requiringRole, requestUser.getMeetingRole()))
                .findAny()
                .orElseThrow(() -> new AuthorizationFailException("회원의 권한이 맞지 않습니다. " +
                        "필요 권한: " + Arrays.toString(requiringRoles) +
                        " 회원 권한: [" + requestUser.getMeetingRole() + "]"));

        return true;
    }

    private boolean authCheck(MeetingRole requiringRole, MeetingRole roleToCheck) {

        if (requiringRole == roleToCheck) {
            return true;
        }

        return false;
    }

    private void checkEmptyMeeting(List<MeetingUserEntity> meetingUsers) {
        // 아예 없는 경우 Meeting 자체가 없음(Mandatory 관계이기 때문)
        if (meetingUsers.isEmpty()) {
            throw new CustomException("해당 ID와 일치하는 모임을 찾을 수 없습니다.",
                    ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    private MeetingUserEntity checkUserIncludeAndGet(HttpServletRequest request, List<MeetingUserEntity> meetingUsers) {
        // 요청을 보낸 회원이 해당 Meeting에 가입되어있는지 확인
        MeetingUserEntity requestUser = meetingUsers.stream()
                .filter(mu -> mu.getUserId().equals(getUserId(request)))
                .findAny()
                .orElseThrow(() -> new CustomException("회원이 해당 모임에 가입되어있지 않습니다.",
                        ErrorCode.MEETING_USER_NOT_INCLUDE));

        return requestUser;
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        return TokenUtils.getUserId(token);
    }

    private Long getMeetingId(HttpServletRequest request) {
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        String meetingId = pathVariables.get("meetingId");
        if (Objects.isNull(meetingId)) {
            throw new CustomException("@MeetingAuth는 meeting 리소스가 경로변수에 명시된 경우만 사용 가능합니다.",
                    ErrorCode.AUTHORIZATION_UNABLE);
        }

        try {
            return Long.valueOf(meetingId);
        } catch (NumberFormatException e) {
            throw new CustomException("경로변수의 값 형식이 이상합니다.",
                    ErrorCode.WRONG_PATH_VARIABLE_FORMAT);
        }
    }
}
