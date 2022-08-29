package com.comeon.meetingservice.domain.meetingdate.repository;

import com.comeon.meetingservice.domain.meetingdate.entity.DateUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DateUserRepository extends JpaRepository<DateUserEntity, Long> {

    @Query("select du from DateUserEntity du " +
            "where du.meetingDateEntity.id = :meetingDateId " +
            "and du.meetingUserEntity.id = :meetingUserId")
    Optional<DateUserEntity> findByDateIdAndUserId(@Param("meetingDateId") Long meetingDateId,
                                                   @Param("meetingUserId") Long meetingUserId);

    @Query("select du from DateUserEntity du join fetch du.meetingUserEntity " +
            "where du.meetingDateEntity.id = :meetingDateId")
    List<DateUserEntity> findAllByDateIdFetchUser(@Param("meetingDateId") Long meetingDateId);
}
