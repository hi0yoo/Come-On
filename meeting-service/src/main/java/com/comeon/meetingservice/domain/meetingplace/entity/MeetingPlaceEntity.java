package com.comeon.meetingservice.domain.meetingplace.entity;

import com.comeon.meetingservice.domain.common.BaseEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.EnumType.*;
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

    //@Column(nullable = false)
    private Long apiId;

    @Enumerated(STRING)
    //@Column(nullable = false)
    private PlaceCategory category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String memo;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "orders", nullable = false)
    private Integer order;

    @Builder
    public MeetingPlaceEntity(Long apiId, PlaceCategory category, String name, String address, String memo, Double lat, Double lng, Integer order) {
        this.apiId = apiId;
        this.category = category;
        this.name = name;
        this.address = address;
        this.memo = memo;
        this.lat = lat;
        this.lng = lng;
        this.order = order;
    }

    public void addMeetingEntity(MeetingEntity meetingEntity) {
        this.meetingEntity = meetingEntity;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void updateOrder(Integer order) {
        this.order = order;
    }

    public void updateCategory(PlaceCategory category) {
        this.category = category;
    }

    public void updateInfo(Long apiId, String name, Double lat, Double lng) {
        this.apiId = apiId;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public void increaseOrder() {
        this.order += 1;
    }

    public void decreaseOrder() {
        this.order -= 1;
    }
}
