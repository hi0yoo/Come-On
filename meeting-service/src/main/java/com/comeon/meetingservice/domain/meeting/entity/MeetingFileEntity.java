package com.comeon.meetingservice.domain.meeting.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(name = "meeting_image")
@NoArgsConstructor(access = PROTECTED)
public class MeetingFileEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String storedName;

    @Builder
    private MeetingFileEntity(String originalName, String storedName) {
        this.originalName = originalName;
        this.storedName = storedName;
    }

    public void updateOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void updateStoredName(String storedName) {
        this.storedName = storedName;
    }
}
