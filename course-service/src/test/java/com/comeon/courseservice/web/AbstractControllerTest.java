package com.comeon.courseservice.web;

import com.comeon.courseservice.config.S3MockConfig;
import com.comeon.courseservice.docs.config.RestDocsConfig;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.courselike.entity.CourseLike;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.web.common.file.FileManager;
import com.comeon.courseservice.web.common.file.S3FileManager;
import com.comeon.courseservice.web.common.file.UploadedFileInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang.math.RandomUtils.nextDouble;
import static org.apache.commons.lang.math.RandomUtils.nextInt;

@Import({
        RestDocsConfig.class,
        S3FileManager.class,
        S3MockConfig.class
})
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
public abstract class AbstractControllerTest {

    protected static final String BEARER_TOKEN_TYPE = "Bearer ";

    @Value("${s3.folder-name.course}")
    protected String dirName;

    @SpyBean
    protected FileManager fileManager;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${jwt.secret}")
    protected String jwtSecretKey;

    protected String generateUserAccessToken(Long userId) {
        String userRole = "ROLE_USER";
        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS512)
                .claim("auth", userRole)
                .setIssuer("test")
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plusSeconds(100)))
                .setSubject(userId.toString())
                .compact();
    }

    @BeforeEach
    void setUp(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))
                .alwaysDo(MockMvcResultHandlers.print())
                .alwaysDo(restDocs)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }


    /* ====== controller data ====== */
    private AtomicLong courseIdGenerater = new AtomicLong();
    private AtomicLong coursePlaceIdGenerater = new AtomicLong();
    private AtomicLong courseLikeIdGenerater = new AtomicLong();

    private List<Course> courseList = new ArrayList<>();
    private List<CourseLike> courseLikeList = new ArrayList<>();

    public List<Course> getCourseList() {
        return courseList;
    }

    public List<CourseLike> getCourseLikeList() {
        return courseLikeList;
    }

    public MockMultipartFile getMockMultipartFile(String fileNameWithExt) throws IOException {
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

    public void initData() {
        courseList = setCourses(1L, 15);
        courseList.addAll(setCourses(2L, 15));
        courseList.addAll(setCourses(3L, 15));

        for (Course course : courseList) {
            setCoursePlaces(course, 5);
        }

        Random random = new Random();
        for (Course course : courseList) {
            for (int i = 0; i < 5; i++) {
                setCourseLike(course, (long) (random.nextInt(10) + 1));
            }
        }
    }

    public List<Course> setCourses(Long userId, int count) {
        List<Course> courseList = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            LocalDateTime randomDate = randomDate();
            long courseId = this.courseIdGenerater.incrementAndGet();

            UploadedFileInfo uploadedFileInfo = fileUpload();

            Course course = Course.builder()
                    .userId(userId)
                    .title("courseTitle" + courseId)
                    .description("courseTitle" + courseId)
                    .courseImage(
                            CourseImage.builder()
                                    .originalName(uploadedFileInfo.getOriginalFileName())
                                    .storedName(uploadedFileInfo.getStoredFileName())
                                    .build()
                    )
                    .build();
            ReflectionTestUtils.setField(course, "id", courseId);
            ReflectionTestUtils.setField(course, "createdDate", randomDate);
            ReflectionTestUtils.setField(course, "lastModifiedDate", randomDate);
            courseList.add(course);
        }

        return courseList;
    }

    public void setCoursePlaces(Course course, int count) {
        int size = course.getCoursePlaces().size();
        for (int i = size + 1; i <= size + count; i++) {
            long coursePlaceId = this.coursePlaceIdGenerater.incrementAndGet();
            CoursePlace coursePlace = CoursePlace.builder()
                    .course(course)
                    .name("placeName" + coursePlaceId)
                    .description("placeDescription" + coursePlaceId)
                    .lat(nextDouble() * (38 - 36 + 1) + 36)
                    .lng(nextDouble() * (128 - 126 + 1) + 126)
                    .order(i)
                    .kakaoPlaceId(coursePlaceId)
                    .placeCategory(CoursePlaceCategory.ETC)
                    .build();
            ReflectionTestUtils.setField(coursePlace, "id", coursePlaceId);
            ReflectionTestUtils.setField(coursePlace, "createdDate", course.getCreatedDate());
            ReflectionTestUtils.setField(coursePlace, "lastModifiedDate", course.getLastModifiedDate());
        }
        course.writeComplete();
    }

    public void setCourseLike(Course course, Long userId) {
        courseLikeList.stream()
                .filter(courseLike -> courseLike.getCourse().equals(course) && courseLike.getUserId().equals(userId))
                .findFirst()
                .ifPresentOrElse(
                        courseLikeList::remove,
                        () -> {
                            CourseLike courseLike = CourseLike.builder()
                                    .course(course)
                                    .userId(userId)
                                    .build();
                            ReflectionTestUtils.setField(courseLike, "id", courseLikeIdGenerater.incrementAndGet());
                            int randomHours = nextInt(30);
                            ReflectionTestUtils.setField(courseLike, "createdDate", course.getCreatedDate().plusHours(randomHours));
                            ReflectionTestUtils.setField(courseLike, "lastModifiedDate", course.getLastModifiedDate().plusHours(randomHours));
                            courseLikeList.add(courseLike);
                        }
                );
    }


    private LocalDateTime randomDate() {
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();
        return now.minusDays(random.nextInt(10));
    }

    private UploadedFileInfo fileUpload() {
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
