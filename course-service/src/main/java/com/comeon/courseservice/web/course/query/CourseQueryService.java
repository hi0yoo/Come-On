package com.comeon.courseservice.web.course.query;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.course.response.CourseDetailResponse;
import com.comeon.courseservice.web.user.service.response.UserDetailsResponse;
import com.comeon.courseservice.web.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseQueryService {

    @Value("${s3.folder-name.course}")
    String dirName;

    private final FileManager fileManager;

    private final UserService userService;
    private final CourseQueryRepository courseQueryRepository;

    public CourseDetailResponse getCourseDetails(Long courseId) {
        Course course = courseQueryRepository.findById(courseId)
                .orElseThrow(
                        () -> new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId)
                );

        // TODO 작성 완료되지 않은 코스는 조회 X
        if (!course.isWritingComplete()) {
            throw new CustomException("작성 완료되지 않은 코스입니다. 요청한 코스 식별값 : " + courseId, ErrorCode.ENTITY_NOT_FOUND);
        }

        // TODO 코스 작성자 닉네임 가져오기
        // TODO 탈퇴된 사용자일 경우, 예외처리 필요? 응답에 따른 예외처리?
        UserDetailsResponse userDetailsResponse = userService.getUserDetails(course.getUserId()).getData();
        String writerNickname = userDetailsResponse.getNickname();

        // TODO 코스 이미지 처리
        String fileUrl = fileManager.getFileUrl(course.getCourseImage().getStoredName(), dirName);

        // TODO 조합해서 응답 내보내기
        return new CourseDetailResponse(course, writerNickname, fileUrl);
    }
}
