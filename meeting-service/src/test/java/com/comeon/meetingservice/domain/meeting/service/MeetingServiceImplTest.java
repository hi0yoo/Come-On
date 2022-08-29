package com.comeon.meetingservice.domain.meeting.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.domain.meeting.dto.MeetingModifyDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingRemoveDto;
import com.comeon.meetingservice.domain.meeting.dto.MeetingAddDto;
import com.comeon.meetingservice.domain.meeting.entity.*;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class MeetingServiceImplTest {

    @Autowired
    MeetingService meetingService;

    @Autowired
    EntityManager em;

    @Nested
    @DisplayName("모임 저장 (add)")
    class 모임생성 {

        private MeetingEntity callAddMethodAndFindEntity(MeetingAddDto meetingAddDto) {
            Long savedId = meetingService.add(meetingAddDto);

            em.flush();
            em.clear();

            MeetingEntity meetingEntity = em.find(MeetingEntity.class, savedId);
            return meetingEntity;
        }

        @Nested
        @DisplayName("필요한 모든 데이터가 있다면")
        class 정상흐름 {

            private MeetingAddDto createNormalMeetingDto() {
                return MeetingAddDto.builder()
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
                MeetingAddDto meetingAddDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingAddDto);

                // then
                assertThat(meetingEntity.getTitle()).isEqualTo(meetingAddDto.getTitle());
                assertThat(meetingEntity.getPeriod().getStartDate()).isEqualTo(meetingAddDto.getStartDate());
                assertThat(meetingEntity.getPeriod().getEndDate()).isEqualTo(meetingAddDto.getEndDate());
            }

            @Test
            @DisplayName("모임 코드가 6자리로 만들어진다.")
            public void 모임코드엔티티_입장코드자리수() throws Exception {
                // given
                MeetingAddDto meetingAddDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingAddDto);

                // then
                assertThat(meetingEntity.getMeetingCodeEntity().getInviteCode().length()).isEqualTo(6);
            }

            @Test
            @DisplayName("모임 코드의 유효 기간은 7일 후로 적용된다.")
            public void 모임코드엔티티_유효기간() throws Exception {
                // given
                MeetingAddDto meetingAddDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingAddDto);

                // then
                assertThat(meetingEntity.getMeetingCodeEntity().getExpiredDate()).isEqualTo(LocalDate.now().plusDays(7));
            }

            @Test
            @DisplayName("모임 파일이 정상적으로 등록된다.")
            public void 모임파일엔티티() throws Exception {
                // given
                MeetingAddDto meetingAddDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingAddDto);

                // then
                assertThat(meetingEntity.getMeetingFileEntity().getOriginalName())
                        .isEqualTo(meetingAddDto.getOriginalFileName());
                assertThat(meetingEntity.getMeetingFileEntity().getStoredName())
                        .isEqualTo(meetingAddDto.getStoredFileName());
            }

            @Test
            @DisplayName("모임 회원의 역할이 HOST로 등록된다.")
            public void 모임회원엔티티_HOST등록() throws Exception {
                // given
                MeetingAddDto meetingAddDto = createNormalMeetingDto();

                // when
                MeetingEntity meetingEntity = callAddMethodAndFindEntity(meetingAddDto);


                // then
                assertThat(meetingEntity.getMeetingUserEntities().iterator().next().getMeetingRole())
                        .isEqualTo(MeetingRole.HOST);
            }

        }

        @Nested
        @DisplayName("필수 데이터가 없다면")
        class 예외 {

            private MeetingAddDto createUnusualMeetingDto() {
                return MeetingAddDto.builder()
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
                MeetingAddDto meetingAddDto = createUnusualMeetingDto();

                // when then
                assertThatThrownBy(() -> callAddMethodAndFindEntity(meetingAddDto))
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

    @Nested
    @DisplayName("모임 수정 (modify)")
    class 모임수정 {

        private void callModifyAndClear(MeetingModifyDto modifyingDto) {
            meetingService.modify(modifyingDto);
            em.flush();
            em.clear();
        }

        @Nested
        @DisplayName("정상 흐름일 경우")
        class 정상흐름 {

            MeetingEntity originalEntity;
            String originalFileName = "original";

            String storedFileName = "stored";

            @BeforeEach
            public void initOriEntity() {
                MeetingAddDto meetingAddDto = MeetingAddDto.builder()
                        .courseId(null)
                        .startDate(LocalDate.of(2022, 7, 10))
                        .endDate(LocalDate.of(2022, 8, 10))
                        .userId(1L)
                        .title("타이틀")
                        .originalFileName(originalFileName)
                        .storedFileName(storedFileName)
                        .build();
                Long savedId = meetingService.add(meetingAddDto);
                originalEntity = em.find(MeetingEntity.class, savedId);
            }

            @Test
            @DisplayName("수정할 데이터가 정상적으로 엔티티에 반영된다.")
            public void 정상반영() throws Exception {
                // given
                MeetingModifyDto modifyingDto = MeetingModifyDto.builder()
                        .id(originalEntity.getId())
                        .title("수정")
                        .startDate(LocalDate.of(2022, 8, 20))
                        .endDate(LocalDate.of(2022, 9, 20))
                        .build();
                // when
                callModifyAndClear(modifyingDto);

                MeetingEntity modifiedEntity = em.find(MeetingEntity.class, originalEntity.getId());

                // then
                assertThat(modifiedEntity.getTitle()).isEqualTo(modifyingDto.getTitle());
                assertThat(modifiedEntity.getPeriod().getStartDate()).isEqualTo(modifyingDto.getStartDate());
                assertThat(modifiedEntity.getPeriod().getEndDate()).isEqualTo(modifyingDto.getEndDate());
            }

            @Test
            @DisplayName("사진이 주어진다면 사진도 수정된다.")
            public void 사진_포함() throws Exception {
                // given
                MeetingModifyDto modifyingDto = MeetingModifyDto.builder()
                        .id(originalEntity.getId())
                        .title("수정")
                        .startDate(LocalDate.of(2022, 8, 20))
                        .endDate(LocalDate.of(2022, 9, 20))
                        .storedFileName("storedMod")
                        .originalFileName("oriMod")
                        .build();
                // when
                callModifyAndClear(modifyingDto);

                MeetingEntity modifiedEntity = em.find(MeetingEntity.class, originalEntity.getId());

                // then
                assertThat(modifiedEntity.getMeetingFileEntity().getOriginalName())
                        .isEqualTo(modifyingDto.getOriginalFileName());
                assertThat(modifiedEntity.getMeetingFileEntity().getStoredName())
                        .isEqualTo(modifyingDto.getStoredFileName());
            }

            @Test
            @DisplayName("사진이 주어지지 않는다면 수정되지 않는다.")
            public void 사진_미포함() throws Exception {
                // given
                MeetingModifyDto modifyingDto = MeetingModifyDto.builder()
                        .id(originalEntity.getId())
                        .title("수정")
                        .startDate(LocalDate.of(2022, 8, 20))
                        .endDate(LocalDate.of(2022, 9, 20))
                        .build();
                // when
                callModifyAndClear(modifyingDto);

                MeetingEntity modifiedEntity = em.find(MeetingEntity.class, originalEntity.getId());

                // then
                assertThat(modifiedEntity.getMeetingFileEntity().getOriginalName())
                        .isEqualTo(originalFileName);
                assertThat(modifiedEntity.getMeetingFileEntity().getStoredName())
                        .isEqualTo(storedFileName);
            }

            @Test
            @DisplayName("기간을 변경한다면 해당 범위 내에 존재하지 않는 모임 날짜는 삭제된다.")
            public void 모임날짜검증() throws Exception {
                // given
                MeetingModifyDto modifyingDto = MeetingModifyDto.builder()
                        .id(originalEntity.getId())
                        .title("수정")
                        .startDate(LocalDate.of(2022, 8, 20))
                        .endDate(LocalDate.of(2022, 9, 20))
                        .build();

                // when
                MeetingDateEntity meetingDateEntity = MeetingDateEntity.builder()
                        .date(LocalDate.of(2022, 8, 10))
                        .build();

                meetingDateEntity.addMeetingEntity(originalEntity);
                em.persist(meetingDateEntity);

                callModifyAndClear(modifyingDto);

                MeetingDateEntity findMeetingDate = em.find(MeetingDateEntity.class, meetingDateEntity.getId());

                // then
                assertThat(findMeetingDate).isNull();
            }
        }

        @Nested
        @DisplayName("없는 엔티티를 수정하려 할 경우")
        class 예외 {
            @Test
            @DisplayName("EntityNotFountException이 발생한다.")
            public void 식별자예외() throws Exception {
                // given
                MeetingModifyDto meetingModifyDto = MeetingModifyDto.builder().id(1L).build();
                //

                assertThatThrownBy(() -> meetingService.modify(meetingModifyDto))
                        .isInstanceOf(CustomException.class);
                assertThatThrownBy(() -> meetingService.modify(meetingModifyDto))
                        .hasMessage("해당 ID와 일치하는 모임을 찾을 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("모임 삭제 (remove)")
    class 모임삭제 {

        MeetingEntity meetingEntity;
        MeetingCodeEntity meetingCodeEntity;
        MeetingFileEntity meetingFileEntity;
        MeetingUserEntity hostMeetingUser;

        @BeforeEach
        public void createdEntities() {
            meetingFileEntity = MeetingFileEntity.builder()
                    .originalName("originalName")
                    .storedName("storedName")
                    .build();

            meetingCodeEntity = MeetingCodeEntity.builder()
                    .inviteCode("code")
                    .expiredDay(7)
                    .build();

            meetingEntity = MeetingEntity.builder()
                    .title("title")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(7))
                    .build();

            hostMeetingUser = MeetingUserEntity.builder()
                    .userId(1L)
                    .meetingRole(MeetingRole.HOST)
                    .build();
        }

        private MeetingEntity removeAndFind(MeetingUserEntity meetingUserToDelete) {
            meetingService.remove(MeetingRemoveDto.builder()
                    .id(meetingEntity.getId())
                    .userId(meetingUserToDelete.getUserId())
                    .build());

            em.flush();
            em.clear();

            MeetingEntity afterRemoved = em.find(MeetingEntity.class, meetingEntity.getId());
            return afterRemoved;
        }

        @Nested
        @DisplayName("모임의 회원이 남아있는 경우")
        class 정상흐름_잔여회원존재 {

            MeetingUserEntity participantMeetingUserA;
            MeetingUserEntity participantMeetingUserB;

            @BeforeEach
            public void initEntities() throws InterruptedException {
                participantMeetingUserA = MeetingUserEntity.builder()
                                .userId(2L)
                                .meetingRole(MeetingRole.PARTICIPANT)
                                .build();

                participantMeetingUserB = MeetingUserEntity.builder()
                                .userId(3L)
                                .meetingRole(MeetingRole.PARTICIPANT)
                                .build();

                meetingEntity.addMeetingCodeEntity(meetingCodeEntity);
                meetingEntity.addMeetingFileEntity(meetingFileEntity);
                meetingEntity.addMeetingUserEntity(hostMeetingUser);
                em.persist(meetingEntity);
                em.flush();
                // 저장 순서(생성 시간)에 따라 다음 HOST가 변경되기 때문에, 테스트를 위해 따로 영속 후 시간 대기
                // 한번에 영속할 경우, 쿼리의 순서가 보장되지 않음..
                Thread.sleep(100);

                participantMeetingUserA.addMeetingEntity(meetingEntity);
                em.persist(participantMeetingUserA);
                em.flush();

                Thread.sleep(100);
                participantMeetingUserB.addMeetingEntity(meetingEntity);
                em.persist(participantMeetingUserB);
                em.flush();

                em.clear();
            }

            @Test
            @DisplayName("회원의 모임 탈퇴 처리가 정상적으로 처리된다.")
            public void 모임탈퇴() throws Exception {
                //when
                MeetingEntity afterRemoved = removeAndFind(participantMeetingUserA);

                //then
                assertThat(afterRemoved.getMeetingUserEntities().size()).isEqualTo(2);
                assertThatThrownBy(() -> afterRemoved.getMeetingUserEntities().stream()
                        .filter(mu -> mu.getId().equals(participantMeetingUserA.getId()))
                        .findAny().get())
                        .isInstanceOf(NoSuchElementException.class);
            }

            @Test
            @DisplayName("한 회원이 탈퇴하더라도 모임은 유지된다.")
            public void 모임유지() throws Exception {
                //when
                MeetingEntity afterRemoved = removeAndFind(participantMeetingUserA);

                //then
                assertThat(afterRemoved).isNotNull();
            }

            @Test
            @DisplayName("HOST인 유저가 탈퇴한다면 다음으로 참여한 유저로 HOST가 변경된다.")
            public void HOST변경() throws Exception {
                //when
                MeetingEntity afterRemoved = removeAndFind(hostMeetingUser);

                MeetingUserEntity changedHost = afterRemoved.getMeetingUserEntities().stream()
                        .filter(m -> m.getMeetingRole() == MeetingRole.HOST)
                        .findAny().orElseThrow();

                //then
                assertThat(changedHost.getId()).isEqualTo(participantMeetingUserA.getId());
                assertThat(changedHost.getId()).isNotEqualTo(participantMeetingUserB.getId());
            }
        }

        @Nested
        @DisplayName("모임에 회원이 남아있지 않은 경우")
        class 정상흐름_잔여회원미존재 {

            @BeforeEach
            public void initEntities() {
                meetingEntity.addMeetingCodeEntity(meetingCodeEntity);
                meetingEntity.addMeetingFileEntity(meetingFileEntity);
                meetingEntity.addMeetingUserEntity(hostMeetingUser);

                em.persist(meetingEntity);

                em.flush();
                em.clear();
            }

            @Test
            @DisplayName("모임 자체가 삭제된다.")
            public void 모임삭제() throws Exception {
                // when
                MeetingEntity afterRemoved = removeAndFind(hostMeetingUser);

                // when then
                assertThat(afterRemoved).isNull();
            }

            @Test
            @DisplayName("연관된 엔티티도 삭제된다.")
            public void 모임삭제_연관엔티티() throws Exception {
                // when
                removeAndFind(hostMeetingUser);

                MeetingFileEntity removedMeetingFile = em.find(MeetingFileEntity.class, meetingFileEntity.getId());
                MeetingCodeEntity removedMeetingCode = em.find(MeetingCodeEntity.class, meetingCodeEntity.getId());
                MeetingUserEntity removedMeetingUser = em.find(MeetingUserEntity.class, hostMeetingUser.getId());

                // when then
                assertThat(removedMeetingFile).isNull();
                assertThat(removedMeetingCode).isNull();
                assertThat(removedMeetingUser).isNull();
            }

        }

        @Nested
        @DisplayName("요청 데이터가 잘못된 경우")
        class 예외 {

            @BeforeEach
            public void initEntities() {
                meetingEntity.addMeetingCodeEntity(meetingCodeEntity);
                meetingEntity.addMeetingFileEntity(meetingFileEntity);
                meetingEntity.addMeetingUserEntity(hostMeetingUser);

                em.persist(meetingEntity);

                em.flush();
                em.clear();
            }

            @Test
            @DisplayName("모임 식별자가 잘못 주어진다면 EntityNotFountException이 발생한다.")
            public void 모임식별자예외() throws Exception {
                long invalidMeetingId;

                do {
                    invalidMeetingId = new Random().nextLong();
                } while (meetingEntity.getId().equals(invalidMeetingId));

                // given
                MeetingRemoveDto meetingRemoveDto = MeetingRemoveDto.builder()
                        .id(invalidMeetingId)
                        .userId(hostMeetingUser.getUserId())
                        .build();

                // when then
                assertThatThrownBy(() -> meetingService.remove(meetingRemoveDto))
                        .isInstanceOf(CustomException.class);
                assertThatThrownBy(() -> meetingService.remove(meetingRemoveDto))
                        .hasMessage("해당 ID와 일치하는 모임을 찾을 수 없습니다.");
            }

            @Test
            @DisplayName("회원 식별자가 잘못 주어진다면 EntityNotFountException이 발생한다.")
            public void 회원식별자예외() throws Exception {
                long invalidUserId;
                do {
                    invalidUserId = new Random().nextLong();
                } while (hostMeetingUser.getId().equals(invalidUserId));

                // given
                MeetingRemoveDto meetingRemoveDto = MeetingRemoveDto.builder()
                        .id(meetingEntity.getId())
                        .userId(invalidUserId)
                        .build();

                // when then
                assertThatThrownBy(() -> meetingService.remove(meetingRemoveDto))
                        .isInstanceOf(CustomException.class);
                assertThatThrownBy(() -> meetingService.remove(meetingRemoveDto))
                        .hasMessage("모임에 유저가 속해있지 않습니다.");
            }

        }
    }

}