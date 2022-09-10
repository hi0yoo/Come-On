package com.comeon.meetingservice.web.common.feign.userservice;

import lombok.*;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class UserDetailResponse {

    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private UserStatus status;

}
