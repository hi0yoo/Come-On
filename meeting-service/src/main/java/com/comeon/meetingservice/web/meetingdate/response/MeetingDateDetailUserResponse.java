package com.comeon.meetingservice.web.meetingdate.response;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@JsonInclude(NON_NULL)
public class MeetingDateDetailUserResponse {

    private Long id;
    private String imageLink;
    private String nickname;
    private MeetingRole meetingRole;

    public static MeetingDateDetailUserResponse toResponse(
            MeetingUserEntity meetingUserEntity,
            String nickname,
            String imageLink) {

        return MeetingDateDetailUserResponse.builder()
                .id(meetingUserEntity.getId())
                .imageLink(imageLink)
                .nickname(nickname)
                .meetingRole(meetingUserEntity.getMeetingRole())
                .build();
    }
}
