package com.comeon.userservice.web.user.query;

import com.comeon.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserQueryRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"account", "profileImg"})
    @Query("select u from User u " +
            "where u.id = :userId")
    Optional<User> findByIdFetchAll(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"profileImg"})
    @Query("select u from User u " +
            "where u.id in :userIdList " +
            "order by u.id asc ")
    List<User> findByIdInIdListFetchProfileImg(@Param("userIdList") List<Long> userIdList);

    @EntityGraph(attributePaths = {"account"})
    @Query("select u from User u " +
            "where u.id = :userId")
    Optional<User> findByIdFetchAccount(@Param("userId") Long userId);
}
