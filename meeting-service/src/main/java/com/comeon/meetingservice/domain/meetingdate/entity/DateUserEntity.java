package com.comeon.meetingservice.domain.meetingdate.entity;

import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.util.List;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "date_user")
public class DateUserEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "meeting_date_id")
    private MeetingDateEntity meetingDateEntity;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "meeting_user_id")
    private MeetingUserEntity meetingUserEntity;

    @Builder
    protected DateUserEntity() {
    }

    public void addMeetingDateEntity(MeetingDateEntity meetingDateEntity) {
        this.meetingDateEntity = meetingDateEntity;
        meetingDateEntity.increaseUserCount();
    }

    public void addMeetingUserEntity(MeetingUserEntity meetingUserEntity) {
        this.meetingUserEntity = meetingUserEntity;
    }
}
