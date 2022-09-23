package com.comeon.userservice.web.user.request;

import com.comeon.userservice.domain.user.service.dto.UserAccountDto;
import com.comeon.userservice.web.common.validation.ValidEnum;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSaveRequest {

    @NotBlank
    private String oauthId;

    @ValidEnum(enumClass = OAuthProvider.class)
    private OAuthProvider provider;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "이메일 형식으로 요청해야 합니다. ex) example1@email.com")
    private String email;

    private String profileImgUrl;

    public UserAccountDto toServiceDto() {
        return UserAccountDto.builder()
                .oauthId(oauthId)
                .provider(provider)
                .email(email)
                .name(name)
                .profileImgUrl(profileImgUrl)
                .build();
    }
}
