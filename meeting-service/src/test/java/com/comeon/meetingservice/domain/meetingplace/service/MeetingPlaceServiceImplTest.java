package com.comeon.meetingservice.domain.meetingplace.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.domain.meetingcode.entity.MeetingCodeEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.entity.MeetingFileEntity;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceModifyDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceRemoveDto;
import com.comeon.meetingservice.domain.meetingplace.dto.MeetingPlaceAddDto;
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
import java.util.List;
import java.util.stream.Collectors;

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

            private MeetingPlaceEntity callAddMethodAndFineEntity(MeetingPlaceAddDto meetingPlaceAddDto) {
                Long savedId = meetingPlaceService.add(meetingPlaceAddDto);
                em.flush();
                em.clear();

                return em.find(MeetingPlaceEntity.class, savedId);
            }

            @Test
            @DisplayName("모임 장소의 이름, 위도, 경도는 정상적으로 저장이 된다.")
            public void 모임장소엔티티() throws Exception {
                // given
                MeetingPlaceAddDto meetingPlaceAddDto = MeetingPlaceAddDto.builder()
                        .meetingId(meetingEntity.getId())
                        .lat(1.1)
                        .lng(1.2)
                        .name("장소1")
                        .build();

                // when
                MeetingPlaceEntity savedMeetingPlace = callAddMethodAndFineEntity(meetingPlaceAddDto);

                // then
                assertThat(savedMeetingPlace.getName()).isEqualTo(meetingPlaceAddDto.getName());
                assertThat(savedMeetingPlace.getLat()).isEqualTo(meetingPlaceAddDto.getLat());
                assertThat(savedMeetingPlace.getLng()).isEqualTo(meetingPlaceAddDto.getLng());
            }

            @Test
            @DisplayName("모임 장소의 순서는 마지막 장소의 다음 순서로 지정된다.")
            public void 모임장소순서() throws Exception {
                // given
                MeetingPlaceAddDto meetingPlaceAddDto1 = MeetingPlaceAddDto.builder()
                        .meetingId(meetingEntity.getId())
                        .lat(1.1)
                        .lng(1.2)
                        .name("장소1")
                        .build();

                MeetingPlaceAddDto meetingPlaceAddDto2 = MeetingPlaceAddDto.builder()
                        .meetingId(meetingEntity.getId())
                        .lat(2.1)
                        .lng(2.2)
                        .name("장소2")
                        .build();

                // when
                MeetingPlaceEntity savedMeetingPlace1 = callAddMethodAndFineEntity(meetingPlaceAddDto1);
                MeetingPlaceEntity savedMeetingPlace2 = callAddMethodAndFineEntity(meetingPlaceAddDto2);

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
            public void 식별자예외() throws Exception {
                // given
                MeetingPlaceAddDto meetingPlaceAddDto = MeetingPlaceAddDto.builder()
                        .meetingId(1L)
                        .lat(1.1)
                        .lng(1.2)
                        .name("장소1")
                        .build();

                // when // then
                assertThatThrownBy(() -> meetingPlaceService.add(meetingPlaceAddDto))
                        .isInstanceOf(CustomException.class);

                assertThatThrownBy(() -> meetingPlaceService.add(meetingPlaceAddDto))
                        .hasMessage("해당 ID와 일치하는 모임을 찾을 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("모임 장소 저장 (modify)")
    class 모임장소수정 {

        @Nested
        @DisplayName("정상 흐름일 경우")
        class 정상흐름 {

            MeetingEntity meetingEntity;
            MeetingPlaceEntity meetingPlaceEntity1;

            @BeforeEach
            public void initMeetingAndPlaces() {
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

                meetingPlaceEntity1 = createMeetingPlace(1);
                meetingPlaceEntity1.addMeetingEntity(meetingEntity);
                em.persist(meetingPlaceEntity1);

                em.flush();
                em.clear();
            }

            private MeetingPlaceEntity createMeetingPlace(Integer order) {
                return MeetingPlaceEntity.builder()
                        .name("장소")
                        .lat(1.1)
                        .lng(2.1)
                        .order(order)
                        .build();
            }

            private MeetingPlaceEntity callAddMethodAndFineEntity(MeetingPlaceAddDto meetingPlaceAddDto) {
                Long savedId = meetingPlaceService.add(meetingPlaceAddDto);
                em.flush();
                em.clear();

                return em.find(MeetingPlaceEntity.class, savedId);
            }

            private MeetingPlaceEntity callModifyAndFind(MeetingPlaceModifyDto meetingPlaceModifyDto) {
                meetingPlaceService.modify(meetingPlaceModifyDto);
                em.flush();
                em.clear();

                MeetingPlaceEntity modifiedMeetingPlace =
                        em.find(MeetingPlaceEntity.class, meetingPlaceModifyDto.getId());
                return modifiedMeetingPlace;
            }

            @Test
            @DisplayName("모든 수정 가능한 필드 수정시 정상적으로 수정된다.")
            public void 모든필드수정() throws Exception {
                // given
                MeetingPlaceModifyDto meetingPlaceModifyDto = MeetingPlaceModifyDto.builder()
                        .id(meetingPlaceEntity1.getId())
                        .lat(1.1)
                        .lng(2.1)
                        .order(5)
                        .name("changed name")
                        .memo("memo")
                        .build();

                // when
                MeetingPlaceEntity modifiedMeetingPlace = callModifyAndFind(meetingPlaceModifyDto);

                // then
                assertThat(modifiedMeetingPlace.getName()).isEqualTo(meetingPlaceModifyDto.getName());
                assertThat(modifiedMeetingPlace.getLat()).isEqualTo(meetingPlaceModifyDto.getLat());
                assertThat(modifiedMeetingPlace.getLng()).isEqualTo(meetingPlaceModifyDto.getLng());
                assertThat(modifiedMeetingPlace.getMemo()).isEqualTo(meetingPlaceModifyDto.getMemo());
                assertThat(modifiedMeetingPlace.getOrder()).isEqualTo(meetingPlaceModifyDto.getOrder());
            }

            @Nested()
            @DisplayName("메모를 변경하는 경우")
            class 메모변경 {

                @Test
                @DisplayName("memo가 주어진다면 정상적으로 수정된다.")
                public void 메모수정_정상() throws Exception {
                    // given
                    MeetingPlaceModifyDto meetingPlaceModifyDto = MeetingPlaceModifyDto.builder()
                            .id(meetingPlaceEntity1.getId())
                            .memo("memo")
                            .build();

                    // when
                    MeetingPlaceEntity modifiedMeetingPlace = callModifyAndFind(meetingPlaceModifyDto);

                    // then
                    assertThat(modifiedMeetingPlace.getMemo()).isEqualTo(meetingPlaceModifyDto.getMemo());
                }

                @Test
                @DisplayName("memo만 주어진다면 다른 필드는 수정되지 않는다.")
                public void 메모수정_다른필드() throws Exception {
                    // given
                    MeetingPlaceModifyDto meetingPlaceModifyDto = MeetingPlaceModifyDto.builder()
                            .id(meetingPlaceEntity1.getId())
                            .memo("memo")
                            .build();

                    // when
                    MeetingPlaceEntity modifiedMeetingPlace = callModifyAndFind(meetingPlaceModifyDto);

                    // then
                    assertThat(modifiedMeetingPlace.getName()).isEqualTo(meetingPlaceEntity1.getName());
                    assertThat(modifiedMeetingPlace.getLat()).isEqualTo(meetingPlaceEntity1.getLat());
                    assertThat(modifiedMeetingPlace.getLng()).isEqualTo(meetingPlaceEntity1.getLng());
                    assertThat(modifiedMeetingPlace.getOrder()).isEqualTo(meetingPlaceEntity1.getOrder());
                }

            }

            @Nested()
            @DisplayName("장소 정보를 변경하는 경우 (name, lat, lng)")
            class 장소정보변경 {

                @Test
                @DisplayName("name, lat, lng를 같이 수정할 경우 정상적으로 수정된다.")
                public void 장소정보수정_정상() throws Exception {
                    // given
                    MeetingPlaceModifyDto meetingPlaceModifyDto = MeetingPlaceModifyDto.builder()
                            .id(meetingPlaceEntity1.getId())
                            .lat(10.1)
                            .lng(20.1)
                            .name("changed name")
                            .build();

                    // when
                    MeetingPlaceEntity modifiedMeetingPlace = callModifyAndFind(meetingPlaceModifyDto);

                    // then
                    assertThat(modifiedMeetingPlace.getName()).isEqualTo(meetingPlaceModifyDto.getName());
                    assertThat(modifiedMeetingPlace.getLat()).isEqualTo(meetingPlaceModifyDto.getLat());
                    assertThat(modifiedMeetingPlace.getLng()).isEqualTo(meetingPlaceModifyDto.getLng());
                }

                @Test
                @DisplayName("name, lat, lng만 수정할 경우 다른 필드는 수정되지 않는다.")
                public void 장소정보수정_다른필드() throws Exception {
                    // given
                    MeetingPlaceModifyDto meetingPlaceModifyDto = MeetingPlaceModifyDto.builder()
                            .id(meetingPlaceEntity1.getId())
                            .lat(10.1)
                            .lng(20.1)
                            .name("changed name")
                            .build();

                    // when
                    MeetingPlaceEntity modifiedMeetingPlace = callModifyAndFind(meetingPlaceModifyDto);

                    // then
                    assertThat(modifiedMeetingPlace.getOrder()).isEqualTo(meetingPlaceEntity1.getOrder());
                    assertThat(modifiedMeetingPlace.getMemo()).isEqualTo(meetingPlaceEntity1.getMemo());
                }

                @Test
                @DisplayName("name, lat, lng 중 하나라도 없다면 수정되지 않는다.")
                public void 장소정보수정_반영안됨() throws Exception {
                    // given
                    MeetingPlaceModifyDto meetingPlaceModifyDto = MeetingPlaceModifyDto.builder()
                            .id(meetingPlaceEntity1.getId())
                            .lat(10.1)
                            .name("changed name")
                            .build();

                    // when
                    MeetingPlaceEntity modifiedMeetingPlace = callModifyAndFind(meetingPlaceModifyDto);

                    // then
                    assertThat(modifiedMeetingPlace.getName()).isNotEqualTo(meetingPlaceModifyDto.getName());
                    assertThat(modifiedMeetingPlace.getLat()).isNotEqualTo(meetingPlaceModifyDto.getLat());
                    assertThat(modifiedMeetingPlace.getLng()).isNotEqualTo(meetingPlaceModifyDto.getLng());

                    assertThat(modifiedMeetingPlace.getName()).isEqualTo(meetingPlaceEntity1.getName());
                    assertThat(modifiedMeetingPlace.getLat()).isEqualTo(meetingPlaceEntity1.getLat());
                    assertThat(modifiedMeetingPlace.getLng()).isEqualTo(meetingPlaceEntity1.getLng());
                }
            }

            @Nested
            @DisplayName("순서를 변경하는 경우 (1, 2, 3, 4, 5 의 순서가 있다고 가정")
            class 순서변경 {

                MeetingPlaceEntity meetingPlaceEntity2;
                MeetingPlaceEntity meetingPlaceEntity3;
                MeetingPlaceEntity meetingPlaceEntity4;
                MeetingPlaceEntity meetingPlaceEntity5;

                @BeforeEach
                public void initPlaces() {
                    meetingPlaceEntity2 = createMeetingPlace(2);
                    meetingPlaceEntity3 = createMeetingPlace(3);
                    meetingPlaceEntity4 = createMeetingPlace(4);
                    meetingPlaceEntity5 = createMeetingPlace(5);

                    meetingPlaceEntity2.addMeetingEntity(meetingEntity);
                    meetingPlaceEntity3.addMeetingEntity(meetingEntity);
                    meetingPlaceEntity4.addMeetingEntity(meetingEntity);
                    meetingPlaceEntity5.addMeetingEntity(meetingEntity);

                    em.persist(meetingPlaceEntity2);
                    em.persist(meetingPlaceEntity3);
                    em.persist(meetingPlaceEntity4);
                    em.persist(meetingPlaceEntity5);
                }

                private MeetingPlaceModifyDto createModifyDto(MeetingPlaceEntity modifyingEntity, 
                                                              Integer modifiedOrder) {
                    return MeetingPlaceModifyDto.builder()
                            .id(modifyingEntity.getId())
                            .order(modifiedOrder)
                            .build();
                }

                private List<MeetingPlaceEntity> callModifyAndFindPlaceList(
                        MeetingPlaceModifyDto meetingPlaceModifyDto) {
                    
                    meetingPlaceService.modify(meetingPlaceModifyDto);
                    em.flush();
                    em.clear();

                    return em.createQuery(
                                    "select mp " +
                                            "from MeetingPlaceEntity mp " +
                                            "where mp.meetingEntity.id = :meetingId " +
                                            "order by mp.order asc",
                                    MeetingPlaceEntity.class)
                            .setParameter("meetingId", meetingEntity.getId())
                            .getResultList();
                }
                
                @Test
                @DisplayName("1번 엔티티를 5번으로 변경한다면 다른 엔티티도 오름차순으로 알맞게 변경된다.")
                public void FROM처음_TO끝() throws Exception {
                    // given
                    MeetingPlaceEntity modifyingEntity = meetingPlaceEntity1;
                    Integer modifiedOrder = 5;

                    MeetingPlaceModifyDto meetingPlaceModifyDto
                            = createModifyDto(modifyingEntity, modifiedOrder);

                    // when
                    List<MeetingPlaceEntity> meetingPlaceEntities
                            = callModifyAndFindPlaceList(meetingPlaceModifyDto);

                    // then
                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getOrder)
                            .collect(Collectors.toList()))
                            .containsExactly(new Integer[]{1, 2, 3, 4, 5});

                    assertThat(meetingPlaceEntities.get(modifiedOrder - 1).getId())
                            .isEqualTo(modifyingEntity.getId());

                    Long[] idOrderToMatch = {meetingPlaceEntity2.getId(),
                            meetingPlaceEntity3.getId(),
                            meetingPlaceEntity4.getId(),
                            meetingPlaceEntity5.getId(),
                            meetingPlaceEntity1.getId()};

                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getId)
                            .collect(Collectors.toList()))
                            .containsExactly(idOrderToMatch);
                }

                @Test
                @DisplayName("2번 엔티티를 5번으로 변경한다면 다른 엔티티도 오름차순으로 알맞게 변경된다.")
                public void FROM중간_TO끝() throws Exception {
                    // given
                    MeetingPlaceEntity modifyingEntity = meetingPlaceEntity2;
                    Integer modifiedOrder = 5;

                    MeetingPlaceModifyDto meetingPlaceModifyDto
                            = createModifyDto(modifyingEntity, modifiedOrder);

                    // when
                    List<MeetingPlaceEntity> meetingPlaceEntities
                            = callModifyAndFindPlaceList(meetingPlaceModifyDto);

                    // then
                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getOrder)
                            .collect(Collectors.toList()))
                            .containsExactly(new Integer[]{1, 2, 3, 4, 5});

                    assertThat(meetingPlaceEntities.get(modifiedOrder - 1).getId())
                            .isEqualTo(modifyingEntity.getId());

                    Long[] idOrderToMatch = {meetingPlaceEntity1.getId(),
                            meetingPlaceEntity3.getId(),
                            meetingPlaceEntity4.getId(),
                            meetingPlaceEntity5.getId(),
                            meetingPlaceEntity2.getId()};

                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getId)
                            .collect(Collectors.toList()))
                            .containsExactly(idOrderToMatch);
                }

                @Test
                @DisplayName("5번 엔티티를 1번으로 변경한다면 다른 엔티티도 오름차순으로 알맞게 변경된다.")
                public void FROM끝_TO처음() throws Exception {
                    // given
                    MeetingPlaceEntity modifyingEntity = meetingPlaceEntity5;
                    Integer modifiedOrder = 1;

                    MeetingPlaceModifyDto meetingPlaceModifyDto
                            = createModifyDto(modifyingEntity, modifiedOrder);

                    // when
                    List<MeetingPlaceEntity> meetingPlaceEntities
                            = callModifyAndFindPlaceList(meetingPlaceModifyDto);

                    // then
                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getOrder)
                            .collect(Collectors.toList()))
                            .containsExactly(new Integer[]{1, 2, 3, 4, 5});

                    assertThat(meetingPlaceEntities.get(modifiedOrder - 1).getId())
                            .isEqualTo(modifyingEntity.getId());

                    Long[] idOrderToMatch = {meetingPlaceEntity5.getId(),
                            meetingPlaceEntity1.getId(),
                            meetingPlaceEntity2.getId(),
                            meetingPlaceEntity3.getId(),
                            meetingPlaceEntity4.getId()};

                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getId)
                            .collect(Collectors.toList()))
                            .containsExactly(idOrderToMatch);
                }

                @Test
                @DisplayName("5번 엔티티를 2번으로 변경한다면 다른 엔티티도 오름차순으로 알맞게 변경된다.")
                public void FROM끝_TO중간() throws Exception {
                    // given
                    MeetingPlaceEntity modifyingEntity = meetingPlaceEntity5;
                    Integer modifiedOrder = 2;

                    MeetingPlaceModifyDto meetingPlaceModifyDto
                            = createModifyDto(modifyingEntity, modifiedOrder);

                    // when
                    List<MeetingPlaceEntity> meetingPlaceEntities
                            = callModifyAndFindPlaceList(meetingPlaceModifyDto);

                    // then
                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getOrder)
                            .collect(Collectors.toList()))
                            .containsExactly(new Integer[]{1, 2, 3, 4, 5});

                    assertThat(meetingPlaceEntities.get(modifiedOrder - 1).getId())
                            .isEqualTo(modifyingEntity.getId());

                    Long[] idOrderToMatch = {meetingPlaceEntity1.getId(),
                            meetingPlaceEntity5.getId(),
                            meetingPlaceEntity2.getId(),
                            meetingPlaceEntity3.getId(),
                            meetingPlaceEntity4.getId()};

                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getId)
                            .collect(Collectors.toList()))
                            .containsExactly(idOrderToMatch);
                }

                @Test
                @DisplayName("2번 엔티티를 4번으로 변경한다면 다른 엔티티도 오름차순으로 알맞게 변경된다.")
                public void FROM작은중간_TO큰중간() throws Exception {
                    // given
                    MeetingPlaceEntity modifyingEntity = meetingPlaceEntity2;
                    Integer modifiedOrder = 4;

                    MeetingPlaceModifyDto meetingPlaceModifyDto
                            = createModifyDto(modifyingEntity, modifiedOrder);

                    // when
                    List<MeetingPlaceEntity> meetingPlaceEntities
                            = callModifyAndFindPlaceList(meetingPlaceModifyDto);

                    // then
                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getOrder)
                            .collect(Collectors.toList()))
                            .containsExactly(new Integer[]{1, 2, 3, 4, 5});

                    assertThat(meetingPlaceEntities.get(modifiedOrder - 1).getId())
                            .isEqualTo(modifyingEntity.getId());

                    Long[] idOrderToMatch = {meetingPlaceEntity1.getId(),
                            meetingPlaceEntity3.getId(),
                            meetingPlaceEntity4.getId(),
                            meetingPlaceEntity2.getId(),
                            meetingPlaceEntity5.getId()};

                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getId)
                            .collect(Collectors.toList()))
                            .containsExactly(idOrderToMatch);
                }

                @Test
                @DisplayName("4번 엔티티를 2번으로 변경한다면 다른 엔티티도 오름차순으로 알맞게 변경된다.")
                public void FROM큰중간_TO작은중간() throws Exception {
                    // given
                    MeetingPlaceEntity modifyingEntity = meetingPlaceEntity4;
                    Integer modifiedOrder = 2;

                    MeetingPlaceModifyDto meetingPlaceModifyDto
                            = createModifyDto(modifyingEntity, modifiedOrder);

                    // when
                    List<MeetingPlaceEntity> meetingPlaceEntities
                            = callModifyAndFindPlaceList(meetingPlaceModifyDto);

                    // then
                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getOrder)
                            .collect(Collectors.toList()))
                            .containsExactly(new Integer[]{1, 2, 3, 4, 5});

                    assertThat(meetingPlaceEntities.get(modifiedOrder - 1).getId())
                            .isEqualTo(modifyingEntity.getId());

                    Long[] idOrderToMatch = {meetingPlaceEntity1.getId(),
                            meetingPlaceEntity4.getId(),
                            meetingPlaceEntity2.getId(),
                            meetingPlaceEntity3.getId(),
                            meetingPlaceEntity5.getId()};

                    assertThat(meetingPlaceEntities.stream()
                            .map(MeetingPlaceEntity::getId)
                            .collect(Collectors.toList()))
                            .containsExactly(idOrderToMatch);
                }
            }
        }

        @Nested
        @DisplayName("예외가 발생하는 경우")
        class 예외 {

            @Test
            @DisplayName("없는 장소를 수정하려고 한다면 EntityNotFoundException이 발생한다.")
            public void 식별자예외() throws Exception {
                // given
                MeetingPlaceModifyDto meetingPlaceModifyDto =
                        MeetingPlaceModifyDto.builder().id(1L).build();

                // when // then
                assertThatThrownBy(() -> meetingPlaceService.modify(meetingPlaceModifyDto))
                        .isInstanceOf(CustomException.class);
                assertThatThrownBy(() -> meetingPlaceService.modify(meetingPlaceModifyDto))
                        .hasMessage("해당 ID와 일치하는 모임 장소를 찾을 수 없습니다.");
            }
        }
    }

    @Nested
    @DisplayName("모임 장소 삭제 (remove)")
    class 모임장소삭제 {
        @Nested
        @DisplayName("정상 흐름일 경우")
        class 정상흐름 {

            MeetingEntity meetingEntity;
            MeetingPlaceEntity meetingPlaceEntity1;
            MeetingPlaceEntity meetingPlaceEntity2;
            MeetingPlaceEntity meetingPlaceEntity3;

            @BeforeEach
            public void initMeetingAndPlaces() {
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

                meetingPlaceEntity1 = createMeetingPlace(1);
                meetingPlaceEntity2 = createMeetingPlace(2);
                meetingPlaceEntity3 = createMeetingPlace(3);
                meetingPlaceEntity1.addMeetingEntity(meetingEntity);
                meetingPlaceEntity2.addMeetingEntity(meetingEntity);
                meetingPlaceEntity3.addMeetingEntity(meetingEntity);

                em.persist(meetingPlaceEntity1);
                em.persist(meetingPlaceEntity2);
                em.persist(meetingPlaceEntity3);

                em.flush();
                em.clear();
            }

            private MeetingPlaceEntity createMeetingPlace(Integer order) {
                return MeetingPlaceEntity.builder()
                        .name("장소")
                        .lat(1.1)
                        .lng(2.1)
                        .order(order)
                        .build();
            }

            @Test
            @DisplayName("있는 모임장소를 삭제할 경우 정상적으로 삭제된다.")
            public void 정상_삭제() throws Exception {
                // given
                MeetingPlaceRemoveDto meetingPlaceRemoveDto =
                        MeetingPlaceRemoveDto.builder()
                                .id(meetingPlaceEntity1.getId())
                                .build();

                // when
                meetingPlaceService.remove(meetingPlaceRemoveDto);
                em.flush();
                em.clear();

                MeetingPlaceEntity removedPlace =
                        em.find(MeetingPlaceEntity.class, meetingPlaceEntity1.getId());

                // then
                assertThat(removedPlace).isNull();

            }

            @Test
            @DisplayName("삭제된 모임 장소보다 순서가 늦은 장소의 경우 앞으로 당겨진다.")
            public void 삭제_순서재정렬() throws Exception {
                // given
                MeetingPlaceEntity placeToRemove = meetingPlaceEntity1;
                MeetingPlaceRemoveDto meetingPlaceRemoveDto =
                        MeetingPlaceRemoveDto.builder()
                                .id(placeToRemove.getId())
                                .build();

                // when
                meetingPlaceService.remove(meetingPlaceRemoveDto);
                em.flush();
                em.clear();

                List<MeetingPlaceEntity> remainingPlace = em.createQuery(
                        "select mp from MeetingPlaceEntity mp " +
                                "where mp.meetingEntity.id = :meetingId", MeetingPlaceEntity.class)
                        .setParameter("meetingId", meetingEntity.getId())
                        .getResultList();

                // then
                assertThat(remainingPlace.stream()
                        .map(MeetingPlaceEntity::getOrder)
                        .collect(Collectors.toList()))
                        .containsExactly(new Integer[]{1, 2});


                Long[] idOrderToMatch = {
                        meetingPlaceEntity2.getId(),
                        meetingPlaceEntity3.getId()};

                assertThat(remainingPlace.stream()
                        .map(MeetingPlaceEntity::getId)
                        .collect(Collectors.toList()))
                        .containsExactly(idOrderToMatch);
            }

        }

        @Nested
        @DisplayName("예외가 발생하는 경우")
        class 예외 {

            @Test
            @DisplayName("없는 장소를 삭제하려고 한다면 Exception이 발생한다.")
            public void 모임예외() throws Exception {
                // given
                MeetingPlaceRemoveDto meetingPlaceRemoveDto =
                        MeetingPlaceRemoveDto.builder().id(1L).build();

                // when // then
                assertThatThrownBy(() -> meetingPlaceService.remove(meetingPlaceRemoveDto))
                        .isInstanceOf(CustomException.class);
                assertThatThrownBy(() -> meetingPlaceService.remove(meetingPlaceRemoveDto))
                        .hasMessage("해당 ID와 일치하는 모임 장소를 찾을 수 없습니다.");
            }
        }
    }
}