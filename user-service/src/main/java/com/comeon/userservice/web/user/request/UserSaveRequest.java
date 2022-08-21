package com.comeon.userservice.web.user.request;

import com.comeon.userservice.common.validation.ValidEnum;
import com.comeon.userservice.domain.user.dto.AccountDto;
import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.dto.UserDto;
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

    // TODO 검증 오류 메시지 작성
    @NotBlank
    private String oauthId;

    @ValidEnum(enumClass = OAuthProvider.class, message = "지원하지 않는 Provider 입니다.")
    private OAuthProvider provider;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", message = "이메일 형식으로 요청해야 합니다. ex) example1@email.com")
    private String email;

    private String profileImgUrl;

    public UserDto toServiceDto() {
        return UserDto.builder()
                .accountDto(
                        AccountDto.builder()
                                .oauthId(oauthId)
                                .provider(provider)
                                .email(email)
                                .name(name)
                                .profileImgUrl(profileImgUrl)
                                .build()
                )
                .build();
    }
}
