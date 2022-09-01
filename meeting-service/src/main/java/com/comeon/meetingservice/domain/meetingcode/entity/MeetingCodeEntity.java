package com.comeon.meetingservice.domain.meetingcode.entity;

import com.comeon.meetingservice.domain.common.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import java.time.LocalDate;

import static javax.persistence.FetchType.*;
import static javax.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(name = "meeting_code")
@NoArgsConstructor(access = PROTECTED)
public class MeetingCodeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String inviteCode;

    @Column(nullable = false)
    private LocalDate expiredDate;

    @Builder
    private MeetingCodeEntity(String inviteCode, Integer expiredDay) {
        this.inviteCode = inviteCode;
        this.expiredDate = LocalDate.now().plusDays(expiredDay);
    }

    public void renewCode(String inviteCode, Integer expiredDay) {
        this.inviteCode = inviteCode;
        this.expiredDate = LocalDate.now().plusDays(expiredDay);
    }

}
