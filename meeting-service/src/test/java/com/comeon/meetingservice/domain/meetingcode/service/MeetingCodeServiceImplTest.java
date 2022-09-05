package com.comeon.meetingservice.domain.meetingcode.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingFileEntity;
import com.comeon.meetingservice.domain.meetingcode.dto.MeetingCodeModifyDto;
import com.comeon.meetingservice.domain.meetingcode.entity.MeetingCodeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class MeetingCodeServiceImplTest {

    @Autowired
    EntityManager em;

    @Autowired
    MeetingCodeService meetingCodeService;

    @Autowired
    Environment env;

    @Nested
    @DisplayName("모임 코드 수정 (modify)")
    class 모임코드수정 {

        public MeetingEntity createMeetingWithCustomCode(String code, Integer expiredDay) {

            MeetingFileEntity meetingFileEntity = MeetingFileEntity.builder()
                    .originalName("ori")
                    .storedName("sto")
                    .build();

            MeetingCodeEntity meetingCodeEntity = MeetingCodeEntity.builder()
                    .inviteCode(code)
                    .expiredDay(expiredDay)
                    .build();

            MeetingEntity meetingEntity = MeetingEntity.builder()
                    .title("title")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(7))
                    .build();

            meetingEntity.addMeetingFileEntity(meetingFileEntity);
            meetingEntity.addMeetingCodeEntity(meetingCodeEntity);

            em.persist(meetingEntity);
            em.flush();
            em.clear();

            return meetingEntity;
        }

        @Nested
        @DisplayName("정상 흐름일 경우")
        class 정상흐름 {

            @Test
            @DisplayName("초대 코드가 정상적으로 갱신된다.")
            public void 코드갱신() throws Exception {
                // given
                String beforeCode = "AAAAAA";

                MeetingEntity meetingEntity = createMeetingWithCustomCode(beforeCode, -1);

                MeetingCodeEntity meetingCodeEntity = meetingEntity.getMeetingCodeEntity();

                MeetingCodeModifyDto meetingCodeModifyDto =
                        MeetingCodeModifyDto.builder()
                                .meetingId(meetingEntity.getId())
                                .id(meetingCodeEntity.getId())
                                .build();

                // when
                meetingCodeService.modify(meetingCodeModifyDto);
                em.flush();
                em.clear();

                MeetingCodeEntity modified = em.find(MeetingCodeEntity.class, meetingCodeEntity.getId());

                // then
                assertThat(modified.getInviteCode()).isNotEqualTo(beforeCode);
            }

            @Test
            @DisplayName("초대 코드는 영문 대문자, 숫자, 영문 대문자 + 숫자 조합 형식 중 하나로 갱신된다.")
            public void 코드형식() throws Exception {
                // given
                MeetingEntity meetingEntity = createMeetingWithCustomCode("AAAAAA", -1);

                MeetingCodeEntity meetingCodeEntity = meetingEntity.getMeetingCodeEntity();

                MeetingCodeModifyDto meetingCodeModifyDto =
                        MeetingCodeModifyDto.builder()
                                .meetingId(meetingEntity.getId())
                                .id(meetingCodeEntity.getId())
                                .build();

                // when
                meetingCodeService.modify(meetingCodeModifyDto);
                em.flush();
                em.clear();

                MeetingCodeEntity modified = em.find(MeetingCodeEntity.class, meetingCodeEntity.getId());

                // then
                assertThat(modified.getInviteCode()).matches("^[0-9A-Z]{6}$");
            }

            @Test
            @DisplayName("초대 코드의 만료 기간이 시스템 설정값에 따라 갱신된다.")
            public void 코드만료기간() throws Exception {
                // given
                MeetingEntity meetingEntity = createMeetingWithCustomCode("AAAAAA", -1);

                MeetingCodeEntity meetingCodeEntity = meetingEntity.getMeetingCodeEntity();

                MeetingCodeModifyDto meetingCodeModifyDto =
                        MeetingCodeModifyDto.builder()
                                .meetingId(meetingEntity.getId())
                                .id(meetingCodeEntity.getId())
                                .build();

                // when
                meetingCodeService.modify(meetingCodeModifyDto);
                em.flush();
                em.clear();

                MeetingCodeEntity modified = em.find(MeetingCodeEntity.class, meetingCodeEntity.getId());

                // then
                assertThat(modified.getExpiredDate()).isEqualTo(
                        LocalDate.now().plusDays(Long.valueOf(env.getProperty("meeting-code.expired-day"))));

            }
        }

        @Nested
        @DisplayName("예외가 발생할 경우")
        class 예외 {

            @Test
            @DisplayName("없는 모임 코드 엔티티를 수정할 경우 예외가 발생한다.")
            public void 식별자예외() throws Exception {
                // given
                MeetingCodeModifyDto meetingCodeModifyDto =
                        MeetingCodeModifyDto.builder()
                                .meetingId(10L)
                                .id(10L)
                                .build();

                // when then
                assertThatThrownBy(() -> meetingCodeService.modify(meetingCodeModifyDto))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
            }

            @Test
            @DisplayName("모임 코드의 유효기간이 지나지 않았다면 예외가 발생한다.")
            public void 유효기간예외() throws Exception {
                // given
                MeetingEntity meetingEntity = createMeetingWithCustomCode("AAAAAA", 7);

                MeetingCodeEntity meetingCodeEntity = meetingEntity.getMeetingCodeEntity();

                MeetingCodeModifyDto meetingCodeModifyDto = MeetingCodeModifyDto.builder()
                        .meetingId(meetingEntity.getId())
                        .id(meetingCodeEntity.getId())
                                .build();

                // when then
                assertThatThrownBy(() -> meetingCodeService.modify(meetingCodeModifyDto))
                        .isInstanceOf(CustomException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNEXPIRED_CODE);
            }

        }
    }
}