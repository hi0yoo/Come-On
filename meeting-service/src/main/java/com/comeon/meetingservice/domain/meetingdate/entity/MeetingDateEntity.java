package com.comeon.meetingservice.domain.meetingdate.entity;

import com.comeon.meetingservice.domain.common.BaseEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.EnumType.*;
import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "meeting_date")
@NoArgsConstructor(access = PROTECTED)
public class MeetingDateEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "meeting_id")
    private MeetingEntity meetingEntity;

    @OneToMany(mappedBy = "meetingDateEntity", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<DateUserEntity> dateUserEntities = new ArrayList<>();

    @Column(nullable = false, unique = true, updatable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer userCount;

    @Enumerated(STRING)
    @Column(nullable = false)
    private DateStatus dateStatus;

    @Builder
    private MeetingDateEntity(LocalDate date) {
        this.date = date;
        this.userCount = 0;
        this.dateStatus = DateStatus.UNFIXED;
    }

    public void addMeetingEntity(MeetingEntity meetingEntity) {
        this.meetingEntity = meetingEntity;
    }

    public void addDateUserEntity(DateUserEntity dateUserEntity) {
        this.dateUserEntities.add(dateUserEntity);
        dateUserEntity.addMeetingDateEntity(this);
    }

    public void updateDateStatus(DateStatus dateStatus) {
        this.dateStatus = dateStatus;
    }

    public void increaseUserCount() {
        this.userCount += 1;
    }

    public void decreaseUserCount() {
        this.userCount -= 1;
    }
}
