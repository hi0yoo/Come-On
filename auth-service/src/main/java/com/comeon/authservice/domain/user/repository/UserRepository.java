package com.comeon.authservice.domain.user.repository;

import com.comeon.authservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u " +
            "where u.oauthId = :oauthId and u.provider = :providerName")
    Optional<User> findByOauthIdAndProviderName(
            @Param("oauthId") String oauthId,
            @Param("providerName") String providerName
    );
}
