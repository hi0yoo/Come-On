package com.comeon.meetingservice.domain.meeting.entity;

import com.comeon.meetingservice.domain.common.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private List<MeetingUserEntity> meetingUserEntities = new ArrayList<>();

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
        //meetingFileEntity.addMeetingEntity(this);
    }

    public void addMeetingCodeEntity(MeetingCodeEntity meetingCodeEntity) {
        this.meetingCodeEntity = meetingCodeEntity;
        //meetingCodeEntity.addMeetingEntity(this);
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
