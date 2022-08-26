package com.comeon.meetingservice.domain.meetingdate.service;

import com.comeon.meetingservice.common.exception.CustomException;
import com.comeon.meetingservice.common.exception.ErrorCode;
import com.comeon.meetingservice.domain.meeting.entity.MeetingEntity;
import com.comeon.meetingservice.domain.meeting.repository.MeetingRepository;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateAddDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateModifyDto;
import com.comeon.meetingservice.domain.meetingdate.dto.MeetingDateRemoveDto;
import com.comeon.meetingservice.domain.meetingdate.entity.DateUserEntity;
import com.comeon.meetingservice.domain.meetingdate.entity.MeetingDateEntity;
import com.comeon.meetingservice.domain.meetingdate.repository.DateUserRepository;
import com.comeon.meetingservice.domain.meetingdate.repository.MeetingDateRepository;
import com.comeon.meetingservice.domain.meetinguser.entity.MeetingUserEntity;
import com.comeon.meetingservice.domain.meetinguser.repository.MeetingUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetingDateServiceImpl implements MeetingDateService {

    private final MeetingRepository meetingRepository;
    private final MeetingUserRepository meetingUserRepository;
    private final MeetingDateRepository meetingDateRepository;
    private final DateUserRepository dateUserRepository;

    @Override
    public Long add(MeetingDateAddDto meetingDateAddDto) {
        // Date를 새로 추가하거나, 이미 있는 Date에 User가 추가되는 경우 모두 여기서 처리할 것임
        // (클라이언트는 날짜 선택하기만 하면 끝이기 때문)

        // 만약 이미 있는 Date일 경우에는 해당 Date 엔티티가 반환, 아닌 경우 Date 새로 만들어서 반환
        MeetingDateEntity meetingDateEntity
                = findOrCreateMeetingDate(meetingDateAddDto.getMeetingId(), meetingDateAddDto.getDate());

        // DateUser에 넣기 위한 MeetingUser 찾기
        MeetingUserEntity meetingUserEntity =
                findMeetingUser(meetingDateAddDto.getUserId(), meetingDateAddDto.getMeetingId());

        // 해당 유저가 이미 해당 날짜를 선택했는지 검증
        checkSelection(meetingDateEntity.getId(), meetingUserEntity.getId());

        // DateUser 생성 후 저장
        DateUserEntity dateUserEntity = DateUserEntity.builder().build();
        dateUserEntity.addMeetingDateEntity(meetingDateEntity);
        dateUserEntity.addMeetingUserEntity(meetingUserEntity);

        dateUserRepository.save(dateUserEntity);
        return meetingDateEntity.getId();
    }

    @Override
    public void modify(MeetingDateModifyDto meetingDateModifyDto) {
        MeetingDateEntity meetingDateEntity = findMeetingDate(meetingDateModifyDto.getId());

        meetingDateEntity.updateDateStatus(meetingDateModifyDto.getDateStatus());
    }

    @Override
    public void remove(MeetingDateRemoveDto meetingDateRemoveDto) {
        // 해당 모임 날짜를 선택한 회원이 더 이상 없다면 삭제처리, 아니라면 날짜 회원 엔티티를 삭제처리

        // 해당 날짜를 선택한 날짜 회원 목록 조회
        List<DateUserEntity> dateUserEntities
                = dateUserRepository.findAllByDateIdFetchUser(meetingDateRemoveDto.getId());

        // 만약 회원 목록이 없다면 애초에 모임 날짜가 있을 수 없음 (Mandatory 이기 때문)
        if (dateUserEntities.isEmpty()) {
            throw new CustomException("해당 ID와 일치하는 모임 날짜를 찾을 수 없습니다.",
                    ErrorCode.ENTITY_NOT_FOUND);
        }

        // 회원 목록 중에서 모임 회원의 회원 아이디가 요청을 보낸 회원 아이디랑 일치하는 엔티티 찾기
        DateUserEntity dateUserEntity
                = filterDateUser(dateUserEntities, meetingDateRemoveDto.getUserId());

        // 찾은 날짜 회원 지우기
        dateUserRepository.delete(dateUserEntity);

        // 만약 날짜 회원 목록의 크기가 1이라면, 마지막 남은 회원을 지운 셈이기에 모임 날짜 엔티티 삭제
        if (dateUserEntities.size() <= 1) {
            meetingDateRepository.delete(dateUserEntity.getMeetingDateEntity());

        // 아니라면 날짜를 선택한 회원의 수를 감소시킴
        } else {
            dateUserEntity.getMeetingDateEntity().decreaseUserCount();
        }
    }

    private MeetingDateEntity findOrCreateMeetingDate(Long meetingId, LocalDate date) {
        // 해당 모임에 대해 해당 날짜를 가진 MeetingDate가 이미 있다면 그걸 반환,
        // 아니라면 새롭게 만들어서 반환
        return findExistDateOrNull(meetingId, date)
                .orElseGet(() -> {
                    MeetingEntity meetingEntity = findMeeting(meetingId);

                    // 해당 날짜가 모임의 기간(시작일, 종료일)내의 날짜인지 확인
                    if (!meetingEntity.getPeriod().isWithinPeriod(date)) {
                        throw new CustomException("날짜가 모임의 기간 내에 포함되지 않습니다.",
                                ErrorCode.DATE_NOT_WITHIN_PERIOD);
                    };

                    MeetingDateEntity newMeetingDate = createMeetingDate(date);

                    newMeetingDate.addMeetingEntity(meetingEntity);
                    meetingDateRepository.save(newMeetingDate);
                    return newMeetingDate;
                });
    }

    private Optional<MeetingDateEntity> findExistDateOrNull(Long meetingId, LocalDate date) {
        return meetingDateRepository.findByMeetingIdAndDate(meetingId, date);
    }

    private MeetingDateEntity createMeetingDate(LocalDate date) {
        return MeetingDateEntity.builder().date(date).build();
    }

    private MeetingEntity findMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("해당 ID와 일치하는 모임을 찾을 수 없습니다.",
                        ErrorCode.ENTITY_NOT_FOUND));
    }

    private MeetingUserEntity findMeetingUser(Long userId, Long meetingId) {
        return meetingUserRepository.findByUserIdAndMeetingId(userId, meetingId)
                .orElseThrow(() -> new CustomException("해당 회원이 모임에 가입되어있지 않습니다.",
                        ErrorCode.MEETING_USER_NOT_INCLUDE));
    }

    private void checkSelection(Long meetingDateId, Long meetingUserId) {
        dateUserRepository.findByDateIdAndUserId(meetingDateId, meetingUserId)
                .ifPresent((du) -> {
                    throw new CustomException("해당 날짜를 이미 회원이 선택했습니다.",
                            ErrorCode.USER_ALREADY_SELECT);
                });
    }

    private MeetingDateEntity findMeetingDate(Long id) {
        return meetingDateRepository.findById(id).orElseThrow(() ->
                new CustomException("해당 ID와 일치하는 모임 날짜를 찾을 수 없습니다.",
                        ErrorCode.ENTITY_NOT_FOUND));
    }

    private DateUserEntity filterDateUser(List<DateUserEntity> dateUserEntities, Long userId) {
        return dateUserEntities.stream()
                .filter((du) ->
                        du.getMeetingUserEntity().getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() ->
                        new CustomException("회원이 해당 날짜를 선택하지 않았습니다.",
                                ErrorCode.USER_NOT_SELECT_DATE));
        // TODO - 어차피 회원이 모임에 속해있는 것은 나중에 인터셉터로 미리 검증을 하게 될 것임..
        // 근데 생각해보니 모임 ID를 안받는데 어떻게 인터셉터에서 검증..??;;;;
        // 이래서 모임 ID를 항상 경로변수로 받아야 하는 건가?......
    }
}
