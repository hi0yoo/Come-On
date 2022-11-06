package com.comeon.courseservice.web;

import com.comeon.courseservice.config.QuerydslConfig;
import com.comeon.courseservice.config.S3MockConfig;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.course.repository.CourseRepository;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courselike.repository.CourseLikeRepository;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.common.file.S3FileManager;
import com.comeon.courseservice.web.common.file.UploadedFileInfo;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.persistence.EntityManager;
import java.io.FileInputStream;
import java.io.IOException;

import static org.apache.commons.lang.math.RandomUtils.nextInt;

@Transactional
@ActiveProfiles("test")
@Import({
        S3MockConfig.class,
        S3FileManager.class,
        QuerydslConfig.class
})
@DataJpaTest(includeFilters = {@ComponentScan.Filter(Repository.class)})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractQueryServiceTest {

    @Value("${s3.folder-name.course}")
    String dirName;

    @Autowired
    protected EntityManager em;

    @Autowired
    protected FileManager fileManager;

    @Autowired
    protected CourseRepository courseRepository;

    @Autowired
    protected CourseLikeRepository courseLikeRepository;

    @BeforeAll
    void initData() {
        int userCount = 1;
        int courseCount = 1;
        int coursePlaceCount = 1;

        // 작성중, 비활성화 코스 추가
        for (int i = 1; i <= 3; i++) {
            int uCount = userCount++;
            for (int c = 1; c <= 3; c++) {
                int cCount = courseCount++;
                UploadedFileInfo uploadedFileInfo = fileUpload();
                // 코스 생성시 작성중 상태로 지정됨.
                Course course = Course.builder()
                        .userId((long) uCount)
                        .title("title" + cCount)
                        .description("description" + cCount)
                        .courseImage(
                                CourseImage.builder()
                                        .originalName(uploadedFileInfo.getOriginalFileName() + cCount)
                                        .storedName(uploadedFileInfo.getStoredFileName() + cCount)
                                        .build()
                        )
                        .build();
                courseRepository.save(course);

                if (i == 2) { // 조건이 맞으면 비활성화 코스로 변경
                    for (int k = 1; k <= 3; k++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) k)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                    course.disabledCourse();
                }
            }
        }

        userCount = 1;
        for (int i = 1; i <= 3; i++) {
            int uCount = userCount++;
            for (int c = 1; c <= 15; c++) {
                int cCount = courseCount++;
                UploadedFileInfo uploadedFileInfo = fileUpload();
                Course course = Course.builder()
                        .userId((long) uCount)
                        .title("title" + cCount)
                        .description("description" + cCount)
                        .courseImage(
                                CourseImage.builder()
                                        .originalName(uploadedFileInfo.getOriginalFileName() + cCount)
                                        .storedName(uploadedFileInfo.getStoredFileName() + cCount)
                                        .build()
                        )
                        .build();

                for (int cp = 1; cp <= 5; cp++) {
                    int cpCount = coursePlaceCount++;
                    CoursePlace.builder()
                            .course(course)
                            .name("placeName" + cpCount)
                            .description("placeDescription" + cpCount)
                            .lat(37.555945 + (cpCount / 1000))
                            .lng(126.972331 + (cpCount / 1000))
                            .address("서울특별시 중구 세종대로 99-" + nextInt(300))
                            .order(cp)
                            .kakaoPlaceId((long) cpCount)
                            .placeCategory(CoursePlaceCategory.ETC)
                            .build();
                }
                course.availableCourse();
                courseRepository.save(course);

                if (c % 3 == 0) {
                    for (int k = 1; k <= 3; k++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) k)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                } else if (c % 7 == 0) {
                    for (int k = 1; k <= 7; k++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) k)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                } else if (c % 10 == 0) {
                    for (int k = 1; k <= 10; k++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) k)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                } else {
                    for (int k = 1; k <= 4; k++) {
                        CourseLike courseLike = CourseLike.builder()
                                .course(course)
                                .userId((long) k)
                                .build();
                        courseLikeRepository.save(courseLike);
                    }
                }
                courseRepository.save(course);
            }
        }
    }

    private MockMultipartFile getMockMultipartFile(String fileNameWithExt) throws IOException {
        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "imgFile",
                fileNameWithExt,
                ContentType.IMAGE_JPEG.getMimeType(),
                new FileInputStream(
                        ResourceUtils.getFile(this.getClass().getResource("/static/" + fileNameWithExt))
                )
        );
        return mockMultipartFile;
    }

    protected UploadedFileInfo fileUpload() {
        MockMultipartFile mockMultipartFile = null;
        try {
            mockMultipartFile = getMockMultipartFile("test-img.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        UploadedFileInfo uploadedFileInfo = fileManager.upload(mockMultipartFile, dirName);
        return uploadedFileInfo;
    }
}
