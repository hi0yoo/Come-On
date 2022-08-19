package com.comeon.meetingservice.domain.meetingplace.service;

import com.comeon.meetingservice.domain.common.exception.EntityNotFoundException;
import com.comeon.meetingservice.domain.meeting.entity.MeetingCodeEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingFileEntity;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceSaveDto;
import com.comeon.meetingservice.domain.meetingplace.entity.MeetingPlaceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class MeetingPlaceServiceImplTest {

    @Autowired
    MeetingPlaceService meetingPlaceService;

    @Autowired
    EntityManager em;

    @Nested
    @DisplayName("모임 장소 저장 (add)")
    class 모임장소생성 {

        @Nested
        @DisplayName("필요한 모든 데이터가 있다면")
        class 정상흐름 {

            MeetingEntity meetingEntity;

            @BeforeEach
            public void initMeeting() {
                MeetingCodeEntity meetingCodeEntity = MeetingCodeEntity.builder()
                        .inviteCode("aaaaaa")
                        .expiredDay(7)
                        .build();

                MeetingFileEntity meetingFileEntity = MeetingFileEntity.builder()
                        .originalName("ori")
                        .storedName("sto")
                        .build();

                meetingEntity = MeetingEntity.builder()
                        .title("title")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(7))
                        .build();

                meetingEntity.addMeetingFileEntity(meetingFileEntity);
                meetingEntity.addMeetingCodeEntity(meetingCodeEntity);

                em.persist(meetingEntity);
                em.flush();
                em.clear();
            }

            private MeetingPlaceEntity callAddMethodAndFineEntity(MeetingPlaceSaveDto meetingPlaceSaveDto) {
                Long savedId = meetingPlaceService.add(meetingPlaceSaveDto);
                em.flush();
                em.clear();

                return em.find(MeetingPlaceEntity.class, savedId);
            }

            @Test
            @DisplayName("모임 장소의 이름, 위도, 경도는 정상적으로 저장이 된다.")
            public void 모임장소엔티티() throws Exception {
                // given
                MeetingPlaceSaveDto meetingPlaceSaveDto = MeetingPlaceSaveDto.builder()
                        .meetingId(meetingEntity.getId())
                        .lat(1.1)
                        .lng(1.2)
                        .name("장소1")
                        .build();

                // when
                MeetingPlaceEntity savedMeetingPlace = callAddMethodAndFineEntity(meetingPlaceSaveDto);

                // then
                assertThat(savedMeetingPlace.getName()).isEqualTo(meetingPlaceSaveDto.getName());
                assertThat(savedMeetingPlace.getLat()).isEqualTo(meetingPlaceSaveDto.getLat());
                assertThat(savedMeetingPlace.getLng()).isEqualTo(meetingPlaceSaveDto.getLng());
            }

            @Test
            @DisplayName("모임 장소의 순서는 마지막 장소의 다음 순서로 지정된다.")
            public void 모임장소순서() throws Exception {
                // given
                MeetingPlaceSaveDto meetingPlaceSaveDto1 = MeetingPlaceSaveDto.builder()
                        .meetingId(meetingEntity.getId())
                        .lat(1.1)
                        .lng(1.2)
                        .name("장소1")
                        .build();

                MeetingPlaceSaveDto meetingPlaceSaveDto2 = MeetingPlaceSaveDto.builder()
                        .meetingId(meetingEntity.getId())
                        .lat(2.1)
                        .lng(2.2)
                        .name("장소2")
                        .build();

                // when
                MeetingPlaceEntity savedMeetingPlace1 = callAddMethodAndFineEntity(meetingPlaceSaveDto1);
                MeetingPlaceEntity savedMeetingPlace2 = callAddMethodAndFineEntity(meetingPlaceSaveDto2);

                // then
                assertThat(savedMeetingPlace1.getOrder()).isEqualTo(1);
                assertThat(savedMeetingPlace2.getOrder()).isEqualTo(2);
            }
        }

        @Nested
        @DisplayName("필요한 데이터가 유효하지 않는 경우")
        class 예외 {

            @Test
            @DisplayName("없는 모임에 장소를 저장하려고 한다면 EntityNotFoundException이 발생한다.")
            public void 모임예외() throws Exception {
                // given
                MeetingPlaceSaveDto meetingPlaceSaveDto = MeetingPlaceSaveDto.builder()
                        .meetingId(1L)
                        .lat(1.1)
                        .lng(1.2)
                        .name("장소1")
                        .build();

                // when // then
                assertThatThrownBy(() -> meetingPlaceService.add(meetingPlaceSaveDto))
                        .isInstanceOf(EntityNotFoundException.class);
            }
        }
    }
}