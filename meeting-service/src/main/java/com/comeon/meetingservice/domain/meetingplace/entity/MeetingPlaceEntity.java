package com.comeon.meetingservice.domain.meetingplace.entity;

import com.comeon.meetingservice.domain.common.BaseEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "meeting_place")
@NoArgsConstructor(access = PROTECTED)
public class MeetingPlaceEntity extends BaseEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "meeting_id")
    private MeetingEntity meetingEntity;

    @Column(nullable = false)
    private String name;

    private String memo;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "orders", nullable = false)
    private Integer order;

    @Builder
    private MeetingPlaceEntity(String name, String memo, Double lat, Double lng, Integer order) {
        this.name = name;
        this.memo = memo;
        this.lat = lat;
        this.lng = lng;
        this.order = order;
    }

    public void addMeetingEntity(MeetingEntity meetingEntity) {
        this.meetingEntity = meetingEntity;
    }

    public void updateOrder(Integer order) {
        this.order = order;
    }
}
