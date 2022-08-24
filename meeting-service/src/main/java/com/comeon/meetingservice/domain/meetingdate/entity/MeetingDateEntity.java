package com.comeon.meetingservice.domain.meetingdate.entity;

import com.comeon.meetingservice.domain.common.BaseEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.Date;

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

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private Integer userCount;

    @Enumerated(STRING)
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
