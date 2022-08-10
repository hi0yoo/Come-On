package com.comeon.meetingservice.domain.meeting.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(name = "user_meeting")
@NoArgsConstructor(access = PROTECTED)
public class MeetingUserEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "meeting_id")
    private MeetingEntity meetingEntity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingRole meetingRole;

    @Builder
    private MeetingUserEntity(Long userId, MeetingRole meetingRole) {
        this.userId = userId;
        this.meetingRole = meetingRole;
    }

    public void addMeetingEntity(MeetingEntity meetingEntity) {
        this.meetingEntity = meetingEntity;
    }
}
