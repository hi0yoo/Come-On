package com.comeon.userservice.web.user.response;

import com.comeon.userservice.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailResponse {

    private Long userId;
    private String nickname;
    private ProfileImgResponse profileImg;
    private String role;

    private String email;
    private String name;

    public UserDetailResponse(User user, String profileImgUrl) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.role = user.getRole().getRoleValue();
        this.email = user.getAccount().getEmail();
        this.name = user.getAccount().getName();
        if (user.getProfileImg() != null) {
            this.profileImg = new ProfileImgResponse(user.getProfileImg().getId(), profileImgUrl);
        }
    }

    @Getter
    public static class ProfileImgResponse {
        private Long id;
        private String imageUrl;

        public ProfileImgResponse(Long id, String imageUrl) {
            this.id = id;
            this.imageUrl = imageUrl;
        }
    }
}
