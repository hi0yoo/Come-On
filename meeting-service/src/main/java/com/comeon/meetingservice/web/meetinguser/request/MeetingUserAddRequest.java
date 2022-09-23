package com.comeon.meetingservice.web.meetinguser.request;

import com.comeon.meetingservice.domain.meetinguser.dto.MeetingUserAddDto;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class MeetingUserAddRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9A-Z]{6}$")
    private String inviteCode;

    public MeetingUserAddDto toDto() {
        return MeetingUserAddDto.builder()
                .inviteCode(inviteCode)
                .build();
    }
}
