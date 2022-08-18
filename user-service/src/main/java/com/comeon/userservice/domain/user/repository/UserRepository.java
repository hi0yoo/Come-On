package com.comeon.userservice.domain.user.repository;

import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u " +
            "where u.oauthId = :oauthId and u.provider = :provider")
    Optional<User> findByOAuthIdAndProvider(
            @Param("oauthId") String oauthId,
            @Param("provider") OAuthProvider provider
    );
}