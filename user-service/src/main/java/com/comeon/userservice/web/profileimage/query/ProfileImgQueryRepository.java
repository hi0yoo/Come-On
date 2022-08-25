package com.comeon.userservice.web.profileimage.query;

import com.comeon.userservice.domain.profileimage.entity.ProfileImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileImgQueryRepository extends JpaRepository<ProfileImg, Long> {

    @Query("select pi from ProfileImg pi " +
            "where pi.user.id = :userId")
    Optional<ProfileImg> findByUserId(@Param("userId") Long userId);
}
