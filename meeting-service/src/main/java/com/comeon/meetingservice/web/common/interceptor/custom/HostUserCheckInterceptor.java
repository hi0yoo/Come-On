package com.comeon.meetingservice.web.common.interceptor.custom;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.web.common.util.TokenUtils;
import com.comeon.meetingservice.web.meetinguser.query.MeetingUserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class HostUserCheckInterceptor implements HandlerInterceptor {

    private final MeetingUserQueryRepository meetingUserQueryRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("[HostUserCheckInterceptor] 발동");

        Long meetingId = getMeetingId(request);

        List<MeetingUserEntity> meetingUsers = meetingUserQueryRepository.findAllByMeetingId(meetingId);

        // 없는 모임 리소스인지 확인
        checkEmptyMeeting(meetingUsers);

        // 요청을 보낸 회원이 모임에 가입되었는지 확인 및 구해오기
        MeetingUserEntity requestUser = checkUserIncludeAndGet(request, meetingUsers);

        // 해당 회원이 HOST인지 확인하기
        checkHostUser(requestUser);

        request.getRequestURI();

        return true;
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

    private void checkHostUser(MeetingUserEntity requestUser) {
        if (!requestUser.getMeetingRole().equals(MeetingRole.HOST)) {
            throw new CustomException("해당 회원은 HOST 권한이 없습니다.",
                    ErrorCode.MEETING_USER_NOT_HOST);
        }
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        return TokenUtils.getUserId(token);
    }

    private Long getMeetingId(HttpServletRequest request) {
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return Long.valueOf(pathVariables.get("meetingId"));
    }
}
