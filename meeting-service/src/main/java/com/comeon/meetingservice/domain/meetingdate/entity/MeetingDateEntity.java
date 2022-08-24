package com.comeon.meetingservice.domain.meeting.entity;

import com.comeon.meetingservice.domain.common.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;

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

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer userCount;

    @Builder
    private MeetingDateEntity(LocalDate date, Integer userCount) {
        this.date = date;
        this.userCount = userCount;
    }

    public void addMeetingEntity(MeetingEntity meetingEntity) {
        this.meetingEntity = meetingEntity;
    }
}
