package com.comeon.userservice.domain.user.repository;

import com.comeon.userservice.domain.user.entity.OAuthProvider;
import com.comeon.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"account", "profileImg"})
    @Query("select u from User u " +
            "where u.account.oauthId = :oauthId and u.account.provider = :provider")
    Optional<User> findByOAuthIdAndProvider(
            @Param("oauthId") String oauthId,
            @Param("provider") OAuthProvider provider
    );

    @Override
    @EntityGraph(attributePaths = {"account", "profileImg"})
    @Query("select u from User u " +
            "where u.id = :userId and u.status = 'ACTIVATE'")
    Optional<User> findById(@Param("userId") Long userId);
}