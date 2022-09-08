package com.comeon.courseservice.web.course.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.common.response.SliceResponse;
import com.comeon.courseservice.web.course.query.repository.CourseLikeQueryRepository;
import com.comeon.courseservice.web.course.query.repository.dto.CourseListData;
import com.comeon.courseservice.web.course.query.repository.CourseQueryRepository;
import com.comeon.courseservice.web.course.query.repository.dto.MyPageCourseListData;
import com.comeon.courseservice.web.course.response.CourseDetailResponse;
import com.comeon.courseservice.web.course.response.CourseListResponse;
import com.comeon.courseservice.web.course.response.MyPageCourseListResponse;
import com.comeon.courseservice.web.feign.userservice.UserServiceFeignClient;
import com.comeon.courseservice.web.feign.userservice.response.UserDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseQueryService {

    @Value("${s3.folder-name.course}")
    private String dirName;

    private final FileManager fileManager;

    private final UserServiceFeignClient userServiceFeignClient;
    private final CourseQueryRepository courseQueryRepository;
    private final CourseLikeQueryRepository courseLikeQueryRepository;

    public CourseDetailResponse getCourseDetails(Long courseId, Long userId) {
        Course course = courseQueryRepository.findByIdFetchAll(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
                );

        // 해당 코스 작성자가 아니라면, 작성 완료되지 않은 코스는 조회 X
        if (!(course.getUserId().equals(userId) || course.isWritingComplete())) {
            throw new CustomException("작성 완료되지 않은 코스입니다. 요청한 코스 식별값 : " + courseId, ErrorCode.NO_AUTHORITIES);
        }

        // 코스 작성자 닉네임 가져오기
        // TODO UserService 예외 발생하여 응답 가져오지 못한 경우 처리.
        UserDetailsResponse userDetailsResponse = userServiceFeignClient.getUserDetails(course.getUserId()).getData();
        // TODO 탈퇴한 회원 닉네임 처리
        String writerNickname = userDetailsResponse.getNickname();

        // 코스 이미지 처리
        String fileUrl = fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName);

        // 코스 좋아요 조회
        Long courseLikeId = null;
        if (userId != null) {
            courseLikeId = courseLikeQueryRepository.findByCourseAndUserId(course, userId)
                    .map(CourseLike::getId)
                    .orElse(null);
        }

        // 조합해서 응답 내보내기
        return new CourseDetailResponse(course, writerNickname, fileUrl, courseLikeId);
    }

    // 코스 리스트 조회
    public SliceResponse<CourseListResponse> getCourseList(Long userId,
                                                           CourseCondition courseCondition,
                                                           Pageable pageable) {
        Slice<CourseListData> slice = courseQueryRepository.findCourseSlice(userId, courseCondition, pageable);

        List<Long> writerIds = slice.getContent().stream()
                .map(courseListData -> courseListData.getCourse().getUserId())
                .distinct()
                .collect(Collectors.toList());

        List<UserDetailsResponse> userDetails = userServiceFeignClient.userList(writerIds)
                .getData()
                .getContents();

        // TODO 탈퇴한 회원 닉네임 처리
        Slice<CourseListResponse> courseListResponseSlice = slice.map(
                courseListData -> CourseListResponse.builder()
                        .course(courseListData.getCourse())
                        .coursePlace(courseListData.getCoursePlace())
                        .firstPlaceDistance(courseListData.getDistance())
                        .imageUrl(
                                fileManager.getFileUrl(
                                        courseListData.getCourse().getCourseImage().getStoredName(),
                                        dirName
                                )
                        )
                        .writerNickname(
                                userDetails.stream()
                                        .filter(userDetailsResponse -> userDetailsResponse.getUserId()
                                                .equals(courseListData.getCourse().getUserId())
                                        )
                                        .map(UserDetailsResponse::getNickname)
                                        .findFirst()
                                        .orElse(null) // TODO 로직 수정
                        )
                        .courseLikeId(courseListData.getUserLikeId())
                        .build()
        );

        return SliceResponse.toSliceResponse(courseListResponseSlice);
    }

    // 유저가 등록한 코스 리스트 조회
    public SliceResponse<MyPageCourseListResponse> getMyRegisteredCourseList(Long userId,
                                                                             Pageable pageable) {
        Slice<MyPageCourseListData> myCourseSlice = courseQueryRepository.findMyCourseSlice(userId, pageable);

        // TODO 탈퇴한 회원 처리 및 오류 처리
        UserDetailsResponse userDetailsResponse = userServiceFeignClient.getUserDetails(userId).getData();
        String userNickname = userDetailsResponse.getNickname();

        Slice<MyPageCourseListResponse> slice = myCourseSlice.map(
                myPageCourseListData -> MyPageCourseListResponse.builder()
                        .course(myPageCourseListData.getCourse())
                        .imageUrl(
                                fileManager.getFileUrl(
                                        myPageCourseListData.getCourse().getCourseImage().getStoredName(),
                                        dirName
                                )
                        )
                        .writerNickname(userNickname)
                        .courseLikeId(myPageCourseListData.getUserLikeId())
                        .build()
        );

        return SliceResponse.toSliceResponse(slice);
    }

    // 유저가 좋아요한 코스 리스트 조회
    public SliceResponse<MyPageCourseListResponse> getMyLikedCourseList(Long userId, Pageable pageable) {
        Slice<MyPageCourseListData> myLikedCourseSlice = courseQueryRepository.findMyLikedCourseSlice(userId, pageable);

        List<Long> writerIds = myLikedCourseSlice.getContent().stream()
                .map(courseListData -> courseListData.getCourse().getUserId())
                .distinct()
                .collect(Collectors.toList());

        List<UserDetailsResponse> userDetails = userServiceFeignClient.userList(writerIds)
                .getData()
                .getContents();

        // TODO 탈퇴한 회원 닉네임 처리
        Slice<MyPageCourseListResponse> slice = myLikedCourseSlice.map(
                courseListData -> MyPageCourseListResponse.builder()
                        .course(courseListData.getCourse())
                        .imageUrl(
                                fileManager.getFileUrl(
                                        courseListData.getCourse().getCourseImage().getStoredName(),
                                        dirName
                                )
                        )
                        .writerNickname(
                                userDetails.stream()
                                        .filter(userDetailsResponse -> userDetailsResponse.getUserId()
                                                .equals(courseListData.getCourse().getUserId())
                                        )
                                        .map(UserDetailsResponse::getNickname)
                                        .findFirst()
                                        .orElse(null) // TODO 로직 수정
                        )
                        .courseLikeId(courseListData.getUserLikeId())
                        .build()
        );

        return SliceResponse.toSliceResponse(slice);
    }

    public String getStoredFileName(Long courseId) {
        return courseQueryRepository.findByIdFetchCourseImg(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
                )
                .getCourseImage()
                .getStoredName();
    }
}
