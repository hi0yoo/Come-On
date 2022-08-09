package com.comeon.meetingservice.domain.meeting.entity;

import com.comeon.meetingservice.domain.common.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(name = "meeting")
@NoArgsConstructor(access = PROTECTED)
public class MeetingEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "meetingEntity", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private MeetingFileEntity meetingFileEntity;

    @OneToOne(mappedBy = "meetingEntity", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private MeetingCodeEntity meetingCodeEntity;

    @OneToMany(mappedBy = "meetingEntity", cascade = CascadeType.ALL, orphanRemoval = true)
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
        meetingFileEntity.addMeetingEntity(this);
    }

    public void addMeetingCodeEntity(MeetingCodeEntity meetingCodeEntity) {
        this.meetingCodeEntity = meetingCodeEntity;
        meetingCodeEntity.addMeetingEntity(this);
    }

    public void addMeetingUserEntity(MeetingUserEntity meetingUserEntity) {
        this.meetingUserEntities.add(meetingUserEntity);
        meetingUserEntity.addMeetingEntity(this);
    }
}
