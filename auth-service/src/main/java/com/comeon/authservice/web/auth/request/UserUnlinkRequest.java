package com.comeon.authservice.web.auth.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUnlinkRequest {

    @NotNull(message = "유저의 OauthId를 입력해주세요.")
    private Long userOauthId;
}
