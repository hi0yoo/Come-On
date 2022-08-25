package com.comeon.userservice.web.user.request;

import com.comeon.userservice.domain.user.service.dto.ModifyUserInfoFields;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class UserModifyRequest {

    @NotBlank
    private String nickname;

    public UserModifyRequest(String nickname) {
        this.nickname = nickname;
    }

    public ModifyUserInfoFields toServiceDto() {
        return new ModifyUserInfoFields(nickname);
    }
}
