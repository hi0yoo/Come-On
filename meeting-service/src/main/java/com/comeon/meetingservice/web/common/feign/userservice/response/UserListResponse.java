package com.comeon.meetingservice.web.common.feign.userservice.response;

import lombok.*;

import static lombok.AccessLevel.PRIVATE;

@Getter @Setter
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
public class UserListResponse {

    private Long userId;
    private String nickname;
    private String profileImgUrl;
    private UserStatus status;
}
