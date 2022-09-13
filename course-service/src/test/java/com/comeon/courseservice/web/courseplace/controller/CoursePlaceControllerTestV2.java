package com.comeon.courseservice.web.courseplace.controller;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseImage;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.CoursePlaceService;
import com.comeon.courseservice.web.common.response.ListResponse;
import com.comeon.courseservice.web.courseplace.query.CoursePlaceQueryService;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureRestDocs
@WebMvcTest(CoursePlaceController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class CoursePlaceControllerTestV2 {

    @MockBean
    CoursePlaceService coursePlaceService;

    @MockBean
    CoursePlaceQueryService coursePlaceQueryService;

    @Autowired
    MockMvc mockMvc;

    Course course;

    @Nested
    @DisplayName("코스 장소 리스트 조회")
    class coursePlaceList {

        void initCourseAndPlaces() {
            course = Course.builder()
                    .userId(1L)
                    .title("courseTitle")
                    .description("courseDescription")
                    .courseImage(
                            CourseImage.builder()
                                    .originalName("originalFileName")
                                    .storedName("storedFileName")
                                    .build()
                    )
                    .build();
            ReflectionTestUtils.setField(course, "id", 1L);

            int count = 5;
            String placeName = "placeName";
            String placeDescription = "placeDescription";
            Double placeLat = 12.34;
            Double placeLng = 23.45;
            List<CoursePlace> coursePlaceList = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                CoursePlace coursePlace = CoursePlace.builder()
                        .course(course)
                        .name(placeName + i)
                        .description(placeDescription + i)
                        .lat(placeLat + i)
                        .lng(placeLng + i)
                        .order(i)
                        .kakaoPlaceId((long) i)
                        .placeCategory(CoursePlaceCategory.of("기타"))
                        .build();
                ReflectionTestUtils.setField(coursePlace, "id", (long) i);
                coursePlaceList.add(coursePlace);
            }
        }

        @Test
        @DisplayName("[docs] 존재하는 코스의 식별값이 넘어오면, 해당 코스의 장소 리스트를 응답으로 반환한다.")
        void success() throws Exception {
            // given
            initCourseAndPlaces();
            course.completeWriting();

            given(coursePlaceQueryService.getCoursePlaces(anyLong()))
                    .willReturn(ListResponse.toListResponse(
                                    course.getCoursePlaces().stream()
                                            .map(CoursePlaceDetails::new)
                                            .collect(Collectors.toList())
                            )
                    );

            Long courseId = course.getId();

            // when
            String path = "/courses/{courseId}/course-places";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, courseId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.count").value(course.getCoursePlaces().size()))
                    .andExpect(jsonPath("$.data.contents").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].coursePlaceId").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].name").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].description").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].lat").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].lng").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].order").isNotEmpty());

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("장소 리스트를 조회할 코스의 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("count").type(JsonFieldType.NUMBER).description("조회한 코스 장소 리스트의 수"),
                                    fieldWithPath("contents").type(JsonFieldType.ARRAY).description("코스 장소 리스트"),
                                    fieldWithPath("contents[].coursePlaceId").type(JsonFieldType.NUMBER).description("코스 장소 식별값"),
                                    fieldWithPath("contents[].name").type(JsonFieldType.STRING).description("장소 이름"),
                                    fieldWithPath("contents[].description").type(JsonFieldType.STRING).description("장소 설명"),
                                    fieldWithPath("contents[].lat").type(JsonFieldType.NUMBER).description("장소 위도"),
                                    fieldWithPath("contents[].lng").type(JsonFieldType.NUMBER).description("장소 경도"),
                                    fieldWithPath("contents[].order").type(JsonFieldType.NUMBER).description("장소 순서"),
                                    fieldWithPath("contents[].kakaoPlaceId").type(JsonFieldType.NUMBER).description("Kakao Map에서 장소의 식별값"),
                                    fieldWithPath("contents[].placeCategory").type(JsonFieldType.STRING).description("장소의 카테고리")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 존재하지 않는 코스의 식별값이 넘어오면, http status 400 반환한다.")
        void failNotExistCourseId() throws Exception {
            // given
            given(coursePlaceQueryService.getCoursePlaces(anyLong()))
                    .willThrow(new EntityNotFoundException());

            Long courseId = 100L;

            // when
            String path = "/courses/{courseId}/course-places";
            ResultActions perform = mockMvc.perform(
                    get(path, courseId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.ENTITY_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.ENTITY_NOT_FOUND.getMessage()));

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("예외 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("서버 내부 에러 코드"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("에러에 대한 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("[docs] 작성 완료되지 않은 코스의 식별값이 넘어오면, http status 400 반환한다.")
        void failNotCompleteWriting() throws Exception {
            // given
            initCourseAndPlaces();

            given(coursePlaceQueryService.getCoursePlaces(anyLong()))
                    .willThrow(new CustomException(ErrorCode.CAN_NOT_ACCESS_RESOURCE));

            Long courseId = course.getId();

            // when
            String path = "/courses/{courseId}/course-places";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, courseId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.code").value(ErrorCode.CAN_NOT_ACCESS_RESOURCE.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.CAN_NOT_ACCESS_RESOURCE.getMessage()));

            // docs
            perform.andDo(
                    document(
                            "{class-name}/{method-name}",
                            preprocessRequest(prettyPrint()),
                            preprocessResponse(prettyPrint()),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("예외 응답 필드")),
                                    fieldWithPath("code").type(JsonFieldType.NUMBER).description("서버 내부 에러 코드"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("에러에 대한 메시지")
                            )
                    )
            );
        }
    }
}
