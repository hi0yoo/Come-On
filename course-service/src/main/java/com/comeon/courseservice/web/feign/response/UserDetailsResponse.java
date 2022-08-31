package com.comeon.courseservice.web.feign.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {

    private Long userId;
    private String nickname;
    private String profileImgUrl;
}
