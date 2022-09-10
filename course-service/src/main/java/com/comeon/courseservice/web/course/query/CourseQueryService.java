package com.comeon.courseservice.web.course.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseLike;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.course.query.repository.CourseLikeQueryRepository;
import com.comeon.courseservice.web.course.query.repository.CourseQueryRepository;
import com.comeon.courseservice.web.course.response.CourseDetailResponse;
import com.comeon.courseservice.web.feign.userservice.UserServiceFeignClient;
import com.comeon.courseservice.web.feign.userservice.response.UserDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // TODO 탈퇴된 사용자일 경우, UserService 예외 발생하여 응답 가져오지 못한 경우 처리.
        // TODO 탈퇴된 사용자 응답 변경 -> 여기도 변경할 것
        UserDetailsResponse userDetailsResponse = userServiceFeignClient.getUserDetails(course.getUserId()).getData();
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
}
