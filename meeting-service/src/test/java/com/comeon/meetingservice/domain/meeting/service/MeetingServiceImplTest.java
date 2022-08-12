package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.domain.meeting.dto.MeetingDto;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class MeetingServiceImplTest {

    @Autowired
    MeetingService meetingService;

    @Autowired
    EntityManager em;

    @Nested
    @DisplayName("모임 저장 (add)")
    class 모임생성 {

        private MeetingEntity callAddMethodAndFindEntity(MeetingDto meetingDto) throws IOException {
            Long savedId = meetingService.add(meetingDto);

            em.flush();
            em.clear();

            MeetingEntity meetingEntity = em.find(MeetingEntity.class, savedId);
            return meetingEntity;
        }

        @Nested
        @DisplayName("필요한 모든 데이터가 있다면")
        class 정상흐름 {

            private MeetingDto createNormalMeetingDto() {
                return MeetingDto.builder()
                        .courseId(null)
                        .startDate(LocalDate.of(2022, 7, 10))
                        .endDate(LocalDate.of(2022, 8, 10))
                        .userId(1L)
                        .title("타이틀")
                        .originalFileName("original")
                        .storedFileName("stored")
                        .build();
            }

            @Test
            @DisplayName("모임의 제목, 시작일, 종료일은 정상적으로 저장이 된다.")
            public void 모임엔티티() throws Exception {
                // given
                MeetingDto meetingDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingDto);

                // then
                assertThat(meetingEntity.getTitle()).isEqualTo(meetingDto.getTitle());
                assertThat(meetingEntity.getStartDate()).isEqualTo(meetingDto.getStartDate());
                assertThat(meetingEntity.getEndDate()).isEqualTo(meetingDto.getEndDate());
            }

            @Test
            @DisplayName("모임 코드가 6자리로 만들어진다.")
            public void 모임코드엔티티_입장코드자리수() throws Exception {
                // given
                MeetingDto meetingDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingDto);

                // then
                assertThat(meetingEntity.getMeetingCodeEntity().getInviteCode().length()).isEqualTo(6);
            }

            @Test
            @DisplayName("모임 코드의 유효 기간은 7일 후로 적용된다.")
            public void 모임코드엔티티_유효기간() throws Exception {
                // given
                MeetingDto meetingDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingDto);

                // then
                assertThat(meetingEntity.getMeetingCodeEntity().getExpiredDate()).isEqualTo(LocalDate.now().plusDays(7));
            }

            @Test
            @DisplayName("모임 파일이 정상적으로 등록된다.")
            public void 모임파일엔티티() throws Exception {
                // given
                MeetingDto meetingDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingDto);

                // then
                assertThat(meetingEntity.getMeetingFileEntity().getOriginalName())
                        .isEqualTo(meetingDto.getOriginalFileName());
                assertThat(meetingEntity.getMeetingFileEntity().getStoredName())
                        .isEqualTo(meetingDto.getStoredFileName());
            }

            @Test
            @DisplayName("모임 회원의 역할이 HOST로 등록된다.")
            public void 모임회원엔티티_HOST등록() throws Exception {
                // given
                MeetingDto meetingDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingDto);

                // then
                assertThat(meetingEntity.getMeetingUserEntities().get(0).getMeetingRole()).isEqualTo(MeetingRole.HOST);
            }

        }

        @Nested
        @DisplayName("필수 데이터가 없다면")
        class 필수값예외 {

            private MeetingDto createUnusualMeetingDto() {
                return MeetingDto.builder()
                        .courseId(null)
                        .startDate(LocalDate.of(2022, 7, 10))
                        .endDate(LocalDate.of(2022, 8, 10))
                        .userId(1L)
                        .title(null)
                        .build();
            }

            @Test
            @DisplayName("DataIntegrityViolationException이 발생한다.")
            public void 필수데이터예외() throws Exception {
                // given
                MeetingDto meetingDto = createUnusualMeetingDto();

                // when then
                assertThatThrownBy(() -> callAddMethodAndFindEntity(meetingDto))
                        .isInstanceOf(DataIntegrityViolationException.class);

                // 추후 문제가 생길 가능성이 있음.
                // @Column(nullable=false)로만 검증하기 때문에,
                // Hibernate Validation 의존성을 추가하고, 물리적으로 테이블에 Not Null 제약을 추가하지 않는다면 예외가 발생하지 않음
                // @Column(nullable=false)는 Hibernate Validation 의존성이 추가된 경우 DDL Create 모드일 때 제약조건을 넣어주는 기능만 함
                // 따라서 실제 값이 Null인지 체크하지 않을 가능성이 있기 때문에, 추 후 수정될 가능성이 용이함
                // 사실 Web 계층에서 이미 Null 체크를 할 것이기 때문에, 필요한 테스트케이스인지는 고민해봐야 함
            }
        }

        /* TODO - Course Service 개발 후 처리할 것
        @Nested
        @DisplayName("코스로부터 모임을 생성한다면")
        class 코스모임생성 {
        }
         */

    }

}