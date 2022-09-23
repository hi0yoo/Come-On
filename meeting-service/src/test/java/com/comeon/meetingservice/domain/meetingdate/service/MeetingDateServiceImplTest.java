package com.comeon.meetingservice.domain.meetingdate.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.domain.meetingcode.entity.MeetingCodeEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingFileEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingRole;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateAddDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateModifyDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateRemoveDto;
import com.comeon.meetingservice.domain.meetingdate.entity.DateStatus;
import com.comeon.meetingservice.domain.meetingdate.entity.DateUserEntity;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
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
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class MeetingDateServiceImplTest {

    @Autowired
    MeetingDateService meetingDateService;

    @Autowired
    EntityManager em;

    @Nested
    @DisplayName("모임 날짜 저장 (add)")
    class 모임날짜생성 {

        // Date가 없는 경우 - 정상 흐름
        //      Date가 새로 생성되는지,
        //      해당 Date로 User가 새로 생성되는지,
        // Date가 없는 경우 - 예외
        //      해당 Date가 Meeting의 시작일과 종료일 사이에 위치하지 않으면 예외 발생
        //      모임이 없는 경우
        // Date가 있는 경우 - 정상 흐름
        //      해당 Date로 User가 새로 생성되는지
        //      Date Entity는 새로 생성되지 않는지
        // Date가 있는 경우 - 예외
        //      이미 회원이 모임 날짜를 선택한 경우
        // 공통 예외
        //      회원이 모임에 가입되어있지 않은 경우


        MeetingEntity meetingEntity;
        MeetingUserEntity meetingUserEntity;

        @Nested
        @DisplayName("날짜 엔티티가 이미 있는 경우")
        class 날짜존재 {

            MeetingDateEntity meetingDateEntity;

            @BeforeEach
            public void initEntities() {
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

                meetingUserEntity = MeetingUserEntity.builder()
                        .userId(1L)
                        .meetingRole(MeetingRole.HOST)
                        .build();
                meetingUserEntity.addMeetingEntity(meetingEntity);

                meetingDateEntity = MeetingDateEntity
                        .builder().date(LocalDate.now().plusDays(2)).build();
                meetingDateEntity.addMeetingEntity(meetingEntity);

                em.persist(meetingEntity);
                em.persist(meetingUserEntity);
                em.persist(meetingDateEntity);

                em.flush();
                em.clear();
            }

            @Nested
            @DisplayName("필요한 모든 데이터가 있고, 데이터에 문제가 없다면(정상 흐름이라면)")
            class 정상흐름 {

                @Test
                @DisplayName("해당 날짜, 모임 유저와 관계가 있는 날짜 유저 엔티티가 생성된다.")
                public void 날짜유저생성() throws Exception {
                    // given

                    // 이미 있는 Date엔티티의 Date로 생성
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(meetingDateEntity.getDate())
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when
                    Long savedDateId = meetingDateService.add(meetingDateAddDto);
                    em.flush();
                    em.clear();

                    DateUserEntity dateUserEntity = em.createQuery(
                                    "select du from DateUserEntity du " +
                                            "where du.meetingDateEntity.id = :dateId " +
                                            "and du.meetingUserEntity.id = :userId", DateUserEntity.class)
                            .setParameter("dateId", savedDateId)
                            .setParameter("userId", meetingUserEntity.getId())
                            .getSingleResult();

                    // then
                    assertThat(dateUserEntity).isNotNull();
                    assertThat(dateUserEntity.getMeetingUserEntity().getId())
                            .isEqualTo(meetingUserEntity.getId());
                    assertThat(dateUserEntity.getMeetingDateEntity().getId())
                            .isEqualTo(meetingDateEntity.getId());
                }

                @Test
                @DisplayName("이미 날짜가 있기 때문에 모임 날짜 엔티티는 생성되지 않는다.")
                public void 날짜엔티티미생성() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(meetingDateEntity.getDate())
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when
                    meetingDateService.add(meetingDateAddDto);
                    em.flush();
                    em.clear();

                    // 저장한 날짜와, 모임의 ID를 가지고 엔티티 리스트를 찾음(2개 이면 새로 생성이 되어버린 것, 1개여야 함)
                    List<MeetingDateEntity> MeetingDateEntities = em.createQuery(
                                    "select md from MeetingDateEntity md " +
                                            "where md.meetingEntity.id = :meetingId " +
                                            "and md.date =: date", MeetingDateEntity.class)
                            .setParameter("meetingId", meetingEntity.getId())
                            .setParameter("date", meetingDateAddDto.getDate())
                            .getResultList();

                    // then
                    assertThat(MeetingDateEntities.size()).isEqualTo(1);
                }

                @Test
                @DisplayName("기존 날짜 엔티티의 userCount는 1 증가한다.")
                public void 날짜회원카운트증가() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(meetingDateEntity.getDate())
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when
                    Integer countHolder = meetingDateEntity.getUserCount();

                    Long savedId = meetingDateService.add(meetingDateAddDto);
                    em.flush();
                    em.clear();

                    MeetingDateEntity resultDate = em.find(MeetingDateEntity.class, savedId);

                    // then
                    assertThat(resultDate.getUserCount()).isEqualTo(countHolder + 1);
                }
            }

            @Nested
            @DisplayName("데이터에 문제가 있는 경우")
            class 예외 {

                @Test
                @DisplayName("회원이 모임에 가입되어있지 않으면 예외가 발생한다.")
                public void 회원미가입() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(meetingDateEntity.getDate())
                            .meetingId(meetingEntity.getId())
                            .userId(10000L)
                            .build();

                    // when then
                    assertThatThrownBy(() -> meetingDateService.add(meetingDateAddDto))
                            .isInstanceOf(CustomException.class)
                            .hasMessage("해당 회원이 모임에 가입되어있지 않습니다.");
                }

                @Test
                @DisplayName("이미 해당 회원이 날짜를 이전에 선택했다면 예외가 발생한다.")
                public void 날짜이미존재() throws Exception {
                    // given
                    DateUserEntity dateUserEntity = DateUserEntity
                            .builder().build();
                    dateUserEntity.addMeetingUserEntity(meetingUserEntity);
                    dateUserEntity.addMeetingDateEntity(meetingDateEntity);
                    em.persist(dateUserEntity);

                    // meetingDateService.add(meetingDateAddDto);
                    em.flush();
                    em.clear();

                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(meetingDateEntity.getDate())
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when then
                    assertThatThrownBy(() -> meetingDateService.add(meetingDateAddDto))
                            .isInstanceOf(CustomException.class)
                            .hasMessage("해당 날짜를 이미 회원이 선택했습니다.");
                }
            }
        }

        @Nested
        @DisplayName("날짜가 없는 경우")
        class 날짜미존재 {

            @BeforeEach
            public void initEntities() {
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

                meetingUserEntity = MeetingUserEntity.builder()
                        .userId(1L)
                        .meetingRole(MeetingRole.HOST)
                        .build();
                meetingUserEntity.addMeetingEntity(meetingEntity);

                em.persist(meetingEntity);
                em.persist(meetingUserEntity);

                em.flush();
                em.clear();
            }

            @Nested
            @DisplayName("필요한 모든 데이터가 있고, 데이터에 문제가 없다면(정상 흐름이라면)")
            class 정상흐름 {

                @Test
                @DisplayName("해당 날짜를 가진 모임 날짜 엔티티가 정상적으로 생성된다.")
                public void 날짜엔티티생성() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(LocalDate.now().plusDays(2))
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when
                    Long savedId = meetingDateService.add(meetingDateAddDto);
                    em.flush();
                    em.clear();

                    MeetingDateEntity meetingDateEntity = em.find(MeetingDateEntity.class, savedId);

                    // then
                    assertThat(meetingDateEntity).isNotNull();
                    assertThat(meetingDateEntity.getDate()).isEqualTo(meetingDateAddDto.getDate());
                    assertThat(meetingDateEntity.getDateStatus()).isEqualTo(DateStatus.UNFIXED);
                    assertThat(meetingDateEntity.getUserCount()).isEqualTo(1);
                }

                @Test
                @DisplayName("DateStatus는 UNFIXED로 생성된다.")
                public void 날짜상태() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(LocalDate.now().plusDays(2))
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when
                    Long savedId = meetingDateService.add(meetingDateAddDto);
                    em.flush();
                    em.clear();

                    MeetingDateEntity meetingDateEntity = em.find(MeetingDateEntity.class, savedId);

                    // then
                    assertThat(meetingDateEntity.getDateStatus()).isEqualTo(DateStatus.UNFIXED);
                }

                @Test
                @DisplayName("UserCount가 1로 생성된다.")
                public void 날짜회원카운트증가() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(LocalDate.now().plusDays(2))
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when
                    Long savedId = meetingDateService.add(meetingDateAddDto);
                    em.flush();
                    em.clear();

                    MeetingDateEntity meetingDateEntity = em.find(MeetingDateEntity.class, savedId);

                    // then
                    assertThat(meetingDateEntity.getUserCount()).isEqualTo(1);
                }

                @Test
                @DisplayName("날짜 유저 엔티티가 새로 생성된다.")
                public void 날짜유저생성() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(LocalDate.now().plusDays(2))
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when
                    Long savedDateId = meetingDateService.add(meetingDateAddDto);
                    em.flush();
                    em.clear();

                    DateUserEntity dateUserEntity = em.createQuery(
                                    "select du from DateUserEntity du " +
                                            "where du.meetingDateEntity.id = :dateId " +
                                            "and du.meetingUserEntity.id = :userId", DateUserEntity.class)
                            .setParameter("dateId", savedDateId)
                            .setParameter("userId", meetingUserEntity.getId())
                            .getSingleResult();

                    // then
                    assertThat(dateUserEntity).isNotNull();
                    assertThat(dateUserEntity.getMeetingUserEntity().getId())
                            .isEqualTo(meetingUserEntity.getId());
                    assertThat(dateUserEntity.getMeetingDateEntity().getId())
                            .isEqualTo(savedDateId);
                }

            }

            @Nested
            @DisplayName("데이터에 문제가 있는 경우")
            class 예외 {

                @Test
                @DisplayName("날짜를 저장하려는 모임이 없으면 예외가 발생한다.")
                public void 모임미존재() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(LocalDate.now().plusDays(2))
                            .meetingId(10000L)
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when then
                    assertThatThrownBy(() -> meetingDateService.add(meetingDateAddDto))
                            .isInstanceOf(CustomException.class)
                            .hasMessage("해당 ID와 일치하는 모임을 찾을 수 없습니다.");
                }

                @Test
                @DisplayName("저장하려는 날짜가 모임 기간 내에 포함되지 않으면 예외가 발생한다.")
                public void 기간미포함() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(LocalDate.now().plusDays(30))
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when then
                    assertThatThrownBy(() -> meetingDateService.add(meetingDateAddDto))
                            .isInstanceOf(CustomException.class)
                            .hasMessage("날짜가 모임의 기간 내에 포함되지 않습니다.");
                }

                @DisplayName("회원이 모임에 가입되어있지 않으면 예외가 발생한다.")
                public void 회원미가입() throws Exception {
                    // given
                    MeetingDateAddDto meetingDateAddDto = MeetingDateAddDto.builder()
                            .date(LocalDate.now().plusDays(2))
                            .meetingId(meetingEntity.getId())
                            .userId(meetingUserEntity.getUserId())
                            .build();

                    // when then
                    assertThatThrownBy(() -> meetingDateService.add(meetingDateAddDto))
                            .isInstanceOf(CustomException.class)
                            .hasMessage("해당 회원이 모임에 가입되어있지 않습니다.");
                }
            }
        }
    }

    @Nested
    @DisplayName("모임 날짜 수정 (add)")
    class 모임날짜수정 {

        MeetingEntity meetingEntity;
        MeetingUserEntity meetingUserEntity;

        @BeforeEach
        public void initEntities() {
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

            meetingUserEntity = MeetingUserEntity.builder()
                    .userId(1L)
                    .meetingRole(MeetingRole.HOST)
                    .build();
            meetingUserEntity.addMeetingEntity(meetingEntity);

            em.persist(meetingEntity);
            em.persist(meetingUserEntity);
            em.flush();
            em.clear();
        }

        @Nested
        @DisplayName("정상 흐름일 경우")
        class 정상흐름 {

            @Test
            @DisplayName("날짜 상태가 정상적으로 수정된다.")
            public void 날짜상태() throws Exception {
                // given
                MeetingDateEntity meetingDateEntity = MeetingDateEntity
                        .builder().date(LocalDate.now().plusDays(2)).build();
                meetingDateEntity.addMeetingEntity(meetingEntity);

                em.persist(meetingDateEntity);
                em.flush();
                em.clear();

                MeetingDateModifyDto meetingDateModifyDto = MeetingDateModifyDto.builder()
                        .meetingId(meetingEntity.getId())
                        .id(meetingDateEntity.getId())
                        .dateStatus(DateStatus.FIXED)
                        .build();

                // when
                meetingDateService.modify(meetingDateModifyDto);
                em.flush();
                em.clear();

                MeetingDateEntity modifiedEntity
                        = em.find(MeetingDateEntity.class, meetingDateEntity.getId());

                // then
                assertThat(modifiedEntity.getDateStatus()).isEqualTo(DateStatus.FIXED);
            }
        }

        @Nested
        @DisplayName("예외가 발생하는 경우")
        class 예외 {

            @Test
            @DisplayName("없는 모임 날짜를 수정할 경우 예외가 발생한다.")
            public void 식별자예외() throws Exception {
                // given
                MeetingDateModifyDto meetingDateModifyDto =
                        MeetingDateModifyDto.builder()
                                .id(1000L)
                                .dateStatus(DateStatus.FIXED)
                                .build();

                // when then
                assertThatThrownBy(() -> meetingDateService.modify(meetingDateModifyDto))
                        .isInstanceOf(CustomException.class)
                        .hasMessage("해당 ID와 일치하는 모임 날짜를 찾을 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("모임 날짜 삭제 (remove)")
    class 모임날짜삭제 {

        MeetingEntity meetingEntity;
        MeetingUserEntity meetingUserEntity;

        @BeforeEach
        public void initMeetingAndUser() {
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

            meetingUserEntity = MeetingUserEntity.builder()
                    .userId(1L)
                    .meetingRole(MeetingRole.HOST)
                    .build();
            meetingUserEntity.addMeetingEntity(meetingEntity);

            em.persist(meetingEntity);
            em.persist(meetingUserEntity);
            em.flush();
            em.clear();
        }


        @Nested
        @DisplayName("정상 흐름일 경우")
        class 정상흐름 {

            MeetingDateEntity meetingDateEntity;
            DateUserEntity dateUserEntity;

            @BeforeEach
            public void initDateAndUser() {
                meetingDateEntity = MeetingDateEntity.builder()
                                .date(LocalDate.now().plusDays(2))
                                .build();
                meetingDateEntity.addMeetingEntity(meetingEntity);

                dateUserEntity = DateUserEntity.builder().build();

                dateUserEntity.addMeetingDateEntity(meetingDateEntity);
                dateUserEntity.addMeetingUserEntity(meetingUserEntity);

                em.persist(meetingDateEntity);
                em.persist(dateUserEntity);

                em.flush();
                em.clear();
            }

            @Nested
            @DisplayName("회원이 남아있다면")
            class 잔여회원존재 {

                @BeforeEach
                public void initRemaining() {
                    MeetingUserEntity anotherUser = MeetingUserEntity.builder()
                            .userId(1L)
                            .meetingRole(MeetingRole.HOST)
                            .build();
                    anotherUser.addMeetingEntity(meetingEntity);

                    DateUserEntity remainingUser = DateUserEntity.builder().build();

                    remainingUser.addMeetingDateEntity(meetingDateEntity);
                    remainingUser.addMeetingUserEntity(anotherUser);

                    em.persist(anotherUser);
                    em.merge(meetingDateEntity);
                    em.persist(remainingUser);

                    em.flush();
                    em.clear();
                }

                @Test
                @DisplayName("삭제하려는 DateUser 엔티티가 정상 삭제된다.")
                public void 회원정상삭제() throws Exception {
                    // given
                    MeetingDateRemoveDto meetingDateRemoveDto =
                            MeetingDateRemoveDto.builder()
                                    .meetingId(meetingEntity.getId())
                                    .id(meetingDateEntity.getId())
                                    .userId(meetingUserEntity.getUserId())
                                    .build();

                    // when
                    meetingDateService.remove(meetingDateRemoveDto);
                    em.flush();
                    em.clear();

                    DateUserEntity deletedDateUser = em.find(DateUserEntity.class, dateUserEntity.getId());

                    // then
                    assertThat(deletedDateUser).isNull();
                }

                @Test
                @DisplayName("날짜 엔티티는 삭제되지 않는다.")
                public void 날짜엔티티미삭재() throws Exception {
                    // given
                    MeetingDateRemoveDto meetingDateRemoveDto =
                            MeetingDateRemoveDto.builder()
                                    .meetingId(meetingEntity.getId())
                                    .id(meetingDateEntity.getId())
                                    .userId(meetingUserEntity.getUserId())
                                    .build();

                    // when
                    meetingDateService.remove(meetingDateRemoveDto);
                    em.flush();
                    em.clear();

                    MeetingDateEntity remainingDate
                            = em.find(MeetingDateEntity.class, meetingDateEntity.getId());

                    // then
                    assertThat(remainingDate).isNotNull();
                }

                @Test
                @DisplayName("날짜 엔티티의 userCount가 1 감소한다.")
                public void 유저수감소() throws Exception {
                    // given
                    MeetingDateRemoveDto meetingDateRemoveDto =
                            MeetingDateRemoveDto.builder()
                                    .meetingId(meetingEntity.getId())
                                    .id(meetingDateEntity.getId())
                                    .userId(meetingUserEntity.getUserId())
                                    .build();

                    Integer countHolder = meetingDateEntity.getUserCount();

                    // when
                    meetingDateService.remove(meetingDateRemoveDto);
                    em.flush();
                    em.clear();

                    MeetingDateEntity remainingDate
                            = em.find(MeetingDateEntity.class, meetingDateEntity.getId());

                    // then
                    assertThat(remainingDate.getUserCount()).isEqualTo(countHolder - 1);
                }

            }

            @Nested
            @DisplayName("회원이 남아있지 않다면")
            class 잔여회원미존재 {

                @Test
                @DisplayName("삭제하려는 DateUser 엔티티가 정상 삭제된다.")
                public void 회원정상삭제() throws Exception {
                    // given
                    MeetingDateRemoveDto meetingDateRemoveDto =
                            MeetingDateRemoveDto.builder()
                                    .meetingId(meetingEntity.getId())
                                    .id(meetingDateEntity.getId())
                                    .userId(meetingUserEntity.getUserId())
                                    .build();

                    // when
                    meetingDateService.remove(meetingDateRemoveDto);
                    em.flush();
                    em.clear();

                    DateUserEntity deletedDateUser = em.find(DateUserEntity.class, dateUserEntity.getId());

                    // then
                    assertThat(deletedDateUser).isNull();
                }

                @Test
                @DisplayName("모임 날짜 엔티티도 정상 삭제된다.")
                public void 날짜정상삭제() throws Exception {
                    // given
                    MeetingDateRemoveDto meetingDateRemoveDto =
                            MeetingDateRemoveDto.builder()
                                    .meetingId(meetingEntity.getId())
                                    .id(meetingDateEntity.getId())
                                    .userId(meetingUserEntity.getUserId())
                                    .build();

                    // when
                    meetingDateService.remove(meetingDateRemoveDto);
                    em.flush();
                    em.clear();

                    MeetingDateEntity deletedDate
                            = em.find(MeetingDateEntity.class, meetingDateEntity.getId());

                    // then
                    assertThat(deletedDate).isNull();
                }

            }

        }

        @Nested
        @DisplayName("예외가 발생할 경우")
        class 예외 {

            @Test
            @DisplayName("회원이 해당 날짜를 선택하지 않은 경우 예외가 발생한다.")
            public void 회원미선택() throws Exception {
                // given
                MeetingDateEntity anotherDate = MeetingDateEntity.builder()
                        .date(LocalDate.now().plusDays(4))
                        .build();
                anotherDate.addMeetingEntity(meetingEntity);

                MeetingUserEntity anotherUser = MeetingUserEntity.builder()
                        .userId(5L)
                        .meetingRole(MeetingRole.PARTICIPANT)
                        .build();
                anotherUser.addMeetingEntity(meetingEntity);

                DateUserEntity anotherDateUser = DateUserEntity.builder().build();
                anotherDateUser.addMeetingDateEntity(anotherDate);
                anotherDateUser.addMeetingUserEntity(anotherUser);

                em.persist(anotherDate);
                em.persist(anotherUser);
                em.persist(anotherDateUser);
                em.flush();
                em.clear();

                // 전역변수인 meetingUserEntity의 회원은 anotherDate에 날짜를 선택하지 않았음
                MeetingDateRemoveDto meetingDateRemoveDto = MeetingDateRemoveDto.builder()
                        .meetingId(meetingEntity.getId())
                        .id(anotherDate.getId())
                        .userId(meetingUserEntity.getUserId())
                        .build();

                // when then
                assertThatThrownBy(() -> meetingDateService.remove(meetingDateRemoveDto))
                        .isInstanceOf(CustomException.class)
                        .hasMessage("회원이 해당 날짜를 선택하지 않았습니다.");
            }

            @Test
            @DisplayName("삭제하려는 날짜가 없는 경우 예외가 발생한다.")
            public void 날짜미존재() throws Exception {
                // given
                MeetingDateRemoveDto meetingDateRemoveDto = MeetingDateRemoveDto.builder()
                        .id(1000L)
                        .userId(1000L)
                        .build();

                // when then
                assertThatThrownBy(() -> meetingDateService.remove(meetingDateRemoveDto))
                        .isInstanceOf(CustomException.class)
                        .hasMessage("해당 ID와 일치하는 모임 날짜를 찾을 수 없습니다.");
            }
        }

    }

}