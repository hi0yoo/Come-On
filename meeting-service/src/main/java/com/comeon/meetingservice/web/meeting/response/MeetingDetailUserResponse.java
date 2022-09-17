package com.comeon.meetingservice.web.meeting.response;

import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
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
