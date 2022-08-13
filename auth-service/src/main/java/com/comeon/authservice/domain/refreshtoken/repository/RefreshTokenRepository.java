package com.comeon.authservice.domain.refreshtoken.repository;

import com.comeon.authservice.domain.refreshtoken.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("select rt from RefreshToken rt " +
            "where rt.user.id = :userId")
    Optional<RefreshToken> findByUserId(@Param("userId") Long userId);

    @Query("select rt from RefreshToken rt " +
            "join fetch rt.user " +
            "where rt.token = :token")
    Optional<RefreshToken> findByTokenFetch(@Param("token") String token);

    Optional<RefreshToken> findByToken(String token);
}
