package com.comeon.meetingservice.web.meeting.response;

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
public class MeetingDetailUserResponse {

    private Long id;
    private String nickname;
    private String imageLink;
    private MeetingRole meetingRole;

    public static MeetingDetailUserResponse toResponse(MeetingUserEntity meetingUserEntity,
                                                       String nickName,
                                                       String imageLink) {
        return MeetingDetailUserResponse.builder()
                .id(meetingUserEntity.getId())
                .meetingRole(meetingUserEntity.getMeetingRole())
                .nickname(nickName)
                .imageLink(imageLink)
                .build();
    }
}
