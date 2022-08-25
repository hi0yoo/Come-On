package com.comeon.userservice.web.user.query;

import com.comeon.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserQueryRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"account", "profileImg"})
    @Query("select u from User u " +
            "where u.id = :userId")
    Optional<User> findByIdFetchAll(@Param("userId") Long userId);
}
