package com.comeon.userservice.domain.user.service.config;

import com.comeon.userservice.domain.user.entity.ProfileImg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileImgRepository extends JpaRepository<ProfileImg, Long> {
}
