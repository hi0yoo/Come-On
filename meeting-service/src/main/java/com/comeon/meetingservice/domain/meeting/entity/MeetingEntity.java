package com.comeon.meetingservice.domain.meeting.entity;

import com.comeon.meetingservice.domain.common.BaseEntity;
import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.*;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(name = "meeting")
@NoArgsConstructor(access = PROTECTED)
public class MeetingEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY, cascade = {PERSIST, REMOVE}, orphanRemoval = true, optional = false)
    @JoinColumn(name = "meeting_file_id")
    private MeetingFileEntity meetingFileEntity;

    @OneToOne(fetch = LAZY, cascade = {PERSIST, REMOVE}, orphanRemoval = true, optional = false)
    @JoinColumn(name = "meeting_code_id")
    private MeetingCodeEntity meetingCodeEntity;

    @OneToMany(mappedBy = "meetingEntity", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private Set<MeetingUserEntity> meetingUserEntities = new HashSet<>();

    @OneToMany(mappedBy = "meetingEntity", cascade = {REMOVE}, orphanRemoval = true)
    private Set<MeetingPlaceEntity> meetingPlaceEntities = new HashSet<>();

    @OneToMany(mappedBy = "meetingEntity", cascade = {REMOVE}, orphanRemoval = true)
    private Set<MeetingDateEntity> meetingDateEntities = new HashSet<>();

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String title;

    @Builder
    private MeetingEntity(LocalDate startDate, LocalDate endDate, String title) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.title = title;
    }

    public void addMeetingFileEntity(MeetingFileEntity meetingFileEntity) {
        this.meetingFileEntity = meetingFileEntity;
    }

    public void addMeetingCodeEntity(MeetingCodeEntity meetingCodeEntity) {
        this.meetingCodeEntity = meetingCodeEntity;
    }

    public void addMeetingUserEntity(MeetingUserEntity meetingUserEntity) {
        this.meetingUserEntities.add(meetingUserEntity);
        meetingUserEntity.addMeetingEntity(this);
    }

    public void updateStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void updateEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void updateTitle(String title) {
        this.title = title;
    }
}
