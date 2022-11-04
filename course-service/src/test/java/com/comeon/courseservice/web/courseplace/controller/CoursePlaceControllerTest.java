package com.comeon.courseservice.web.courseplace.controller;

import com.comeon.courseservice.common.exception.CustomException;
import com.comeon.courseservice.common.exception.ErrorCode;
import com.comeon.courseservice.docs.utils.RestDocsUtil;
import com.comeon.courseservice.domain.common.exception.EntityNotFoundException;
import com.comeon.courseservice.domain.course.entity.Course;
import com.comeon.courseservice.domain.course.entity.CourseStatus;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlace;
import com.comeon.courseservice.domain.courseplace.entity.CoursePlaceCategory;
import com.comeon.courseservice.domain.courseplace.service.CoursePlaceService;
import com.comeon.courseservice.web.AbstractControllerTest;
import com.comeon.courseservice.web.common.aop.ValidationAspect;
import com.comeon.courseservice.web.common.response.ListResponse;
import com.comeon.courseservice.web.course.query.CourseQueryService;
import com.comeon.courseservice.web.courseplace.query.CoursePlaceQueryService;
import com.comeon.courseservice.web.courseplace.request.*;
import com.comeon.courseservice.web.courseplace.response.CoursePlaceDetails;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.math.RandomUtils.nextDouble;
import static org.apache.commons.lang.math.RandomUtils.nextInt;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@Import({
        AopAutoConfiguration.class,
        ValidationAspect.class,
        PlaceBatchUpdateRequestValidator.class
})
@WebMvcTest(CoursePlaceController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class CoursePlaceControllerTest extends AbstractControllerTest {

    @MockBean
    CoursePlaceService coursePlaceService;

    @MockBean
    CoursePlaceQueryService coursePlaceQueryService;

    @MockBean
    CourseQueryService courseQueryService;

    @Nested
    @DisplayName("코스 장소 리스트 등록/수정/삭제")
    class coursePlaceUpdateBatch {

        private int order;

        private List<CoursePlaceSaveRequest> generateCoursePlaceSaveRequests(int count) {
            List<CoursePlaceSaveRequest> saveRequests = new ArrayList<>();
            String name = "newName";
            String description = "newDescription";
            Long kakaoPlaceId = 12345L;
            CoursePlaceCategory placeCategory = CoursePlaceCategory.ETC;

            for (int i = 1; i <= count; i++) {
                Double lat = (nextDouble() * (38 - 36 + 1) + 36);
                Double lng = (nextDouble() * (128 - 126 + 1) + 126);
                saveRequests.add(
                        new CoursePlaceSaveRequest(
                                name + i,
                                description + i,
                                lat,
                                lng,
                                "서울특별시 중구 세종대로 99-" + nextInt(300),
                                order++,
                                kakaoPlaceId + i,
                                placeCategory.name()
                        )
                );
            }
            return saveRequests;
        }

        private List<CoursePlaceModifyRequest> generateCoursePlaceModifyRequests(Course course, int count) {
            List<CoursePlace> coursePlaces = course.getCoursePlaces();
            if (coursePlaces.size() < count) {
                throw new RuntimeException("count는 course.coursePlaces.size() 보다 같거나 작아야 합니다.");
            }


            List<CoursePlaceModifyRequest> modifyRequests = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                CoursePlace coursePlace = coursePlaces.get(i);
                CoursePlaceModifyRequest modifyRequest = new CoursePlaceModifyRequest();
                modifyRequest.setId(coursePlace.getId());
                modifyRequest.setOrder(order++);
                modifyRequests.add(modifyRequest);
            }

            return modifyRequests;
        }

        @Test
        @DisplayName("작성중 상태인 코스에 장소들을 추가만 하면 요청이 성공한다.")
        void successSavePlacesOnWritingCourse() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            order = 1;
            List<CoursePlaceSaveRequest> saveRequests = generateCoursePlaceSaveRequests(2);
            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, null, null);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(coursePlaceQueryService.getCoursePlaceIds(courseId))
                    .willReturn(course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList()));
            willDoNothing().given(coursePlaceService)
                    .batchUpdateCoursePlace(eq(courseId), eq(currentUserId), anyList(), anyList(), anyList());
            given(courseQueryService.getCourseStatus(courseId))
                    .willReturn(CourseStatus.COMPLETE);

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseId").value(courseId))
                    .andExpect(jsonPath("$.data.courseStatus").value(CourseStatus.COMPLETE.name()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("장소 데이터 리스트를 변경할 대상 코스 식별값")
                            ),
                            requestFields(
                                    attributes(key("title").value("요청 필드")),
                                    subsectionWithPath("toSave").type(JsonFieldType.ARRAY).description("새로 등록할 장소 데이터 리스트").optional(),
                                    subsectionWithPath("toModify").type(JsonFieldType.ARRAY).description("수정할 기존 장소 데이터 리스트").optional(),
                                    subsectionWithPath("toDelete").type(JsonFieldType.ARRAY).description("삭제할 기존 장소 데이터 리스트").optional()
                            ),
                            requestFields(
                                    beneathPath("toSave").withSubsectionId("toSave"),
                                    attributes(key("title").value("toSave 각 요소의 필드")),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("추가할 장소의 이름"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("추가할 장소에 대한 설명"),
                                    fieldWithPath("lat").type(JsonFieldType.NUMBER).description("추가할 장소의 위도"),
                                    fieldWithPath("lng").type(JsonFieldType.NUMBER).description("추가할 장소의 경도"),
                                    fieldWithPath("order").type(JsonFieldType.NUMBER).description("추가할 장소의 순서"),
                                    fieldWithPath("apiId").type(JsonFieldType.NUMBER).description("추가할 장소의 Kakao-Place ID"),
                                    fieldWithPath("category").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.PLACE_CATEGORY)),
                                    fieldWithPath("address").type(JsonFieldType.STRING).description("장소의 주소").optional()
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("코스 장소 리스트를 변경한 대상 코스 식별값"),
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("코스 장소 리스트 변경 성공 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("작성중 상태인 코스에 수정할 장소를 넣어 요청하면 오류가 발생한다.")
        void errorModifyPlacesOnWritingCourse() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            order = 1;
            List<CoursePlaceSaveRequest> saveRequests = generateCoursePlaceSaveRequests(2);
            List<CoursePlaceModifyRequest> modifyRequests = new ArrayList<>();
            modifyRequests.add(new CoursePlaceModifyRequest(1000L, null, null, null));
            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, modifyRequests, null);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.VALIDATION_FAIL.getCode()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("작성중 상태인 코스에 삭제할 장소를 넣어 요청하면 오류가 발생한다.")
        void errorDeletePlacesOnWritingCourse() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            order = 1;
            List<CoursePlaceSaveRequest> saveRequests = generateCoursePlaceSaveRequests(2);
            List<CoursePlaceDeleteRequest> deleteRequests = new ArrayList<>();
            deleteRequests.add(new CoursePlaceDeleteRequest(1000L));
            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, null, deleteRequests);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.VALIDATION_FAIL.getCode()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("작성 완료된 코스의 장소 리스트 변경의 경우, 요청 데이터 검증에 성공하고, " +
                "update에 성공하면 http status 200, 요청 성공 메시지를 응답한다.")
        void success() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            setCoursePlaces(course, 5);

            // 추가할 데이터
            List<CoursePlaceSaveRequest> saveRequests = new ArrayList<>();
            saveRequests.add(
                    new CoursePlaceSaveRequest(
                            "newName",
                            "newDescription",
                            (nextDouble() * (38 - 36 + 1) + 36),
                            (nextDouble() * (128 - 126 + 1) + 126),
                            "서울특별시 중구 세종대로 99-" + nextInt(300),
                            2,
                            12345L,
                            CoursePlaceCategory.ETC.name()
                    )
            );

            // 수정할 데이터
            List<CoursePlaceModifyRequest> modifyRequests = new ArrayList<>();
            modifyRequests.add(new CoursePlaceModifyRequest(course.getCoursePlaces().get(1).getId(), null, 3, null));
            modifyRequests.add(new CoursePlaceModifyRequest(course.getCoursePlaces().get(2).getId(), null, 4, null));
            modifyRequests.add(new CoursePlaceModifyRequest(course.getCoursePlaces().get(3).getId(), null, 5, null));

            // 삭제할 데이터
            List<CoursePlaceDeleteRequest> deleteRequests = new ArrayList<>();
            deleteRequests.add(new CoursePlaceDeleteRequest(course.getCoursePlaces().get(4).getId()));

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, modifyRequests, deleteRequests);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(coursePlaceQueryService.getCoursePlaceIds(courseId))
                    .willReturn(course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList()));
            willDoNothing().given(coursePlaceService)
                    .batchUpdateCoursePlace(eq(courseId), eq(currentUserId), anyList(), anyList(), anyList());
            given(courseQueryService.getCourseStatus(courseId))
                    .willReturn(CourseStatus.COMPLETE);

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseId").value(courseId))
                    .andExpect(jsonPath("$.data.courseStatus").value(CourseStatus.COMPLETE.name()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            requestHeaders(
                                    attributes(key("title").value("요청 헤더")),
                                    headerWithName(HttpHeaders.AUTHORIZATION).description("로그인 및 토큰 재발급을 통해 발급받은 Bearer AccessToken")
                            ),
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("장소 데이터 리스트를 변경할 대상 코스 식별값")
                            ),
                            requestFields(
                                    attributes(key("title").value("요청 필드")),
                                    subsectionWithPath("toSave").type(JsonFieldType.ARRAY).description("새로 등록할 장소 데이터 리스트").optional(),
                                    subsectionWithPath("toModify").type(JsonFieldType.ARRAY).description("수정할 기존 장소 데이터 리스트").optional(),
                                    subsectionWithPath("toDelete").type(JsonFieldType.ARRAY).description("삭제할 기존 장소 데이터 리스트").optional()
                            ),
                            requestFields(
                                    beneathPath("toSave").withSubsectionId("toSave"),
                                    attributes(key("title").value("toSave 각 요소의 필드")),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("추가할 장소의 이름"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("추가할 장소에 대한 설명"),
                                    fieldWithPath("lat").type(JsonFieldType.NUMBER).description("추가할 장소의 위도"),
                                    fieldWithPath("lng").type(JsonFieldType.NUMBER).description("추가할 장소의 경도"),
                                    fieldWithPath("order").type(JsonFieldType.NUMBER).description("추가할 장소의 순서"),
                                    fieldWithPath("apiId").type(JsonFieldType.NUMBER).description("추가할 장소의 Kakao-Place ID"),
                                    fieldWithPath("category").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.PLACE_CATEGORY)),
                                    fieldWithPath("address").type(JsonFieldType.STRING).description("장소의 주소").optional()
                            ),
                            requestFields(
                                    beneathPath("toModify").withSubsectionId("toModify"),
                                    attributes(key("title").value("toModify 각 요소의 필드")),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("수정할 장소의 식별값"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("수정할 장소에 대한 설명").optional(),
                                    fieldWithPath("order").type(JsonFieldType.NUMBER).description("수정할 장소의 순서").optional(),
                                    fieldWithPath("category").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.PLACE_CATEGORY)).optional()
                            ),
                            requestFields(
                                    beneathPath("toDelete").withSubsectionId("toDelete"),
                                    attributes(key("title").value("toDelete 각 요소의 필드")),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("삭제할 장소의 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("courseId").type(JsonFieldType.NUMBER).description("코스 장소 리스트를 변경한 대상 코스 식별값"),
                                    fieldWithPath("courseStatus").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.COURSE_STATUS)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("코스 장소 리스트 변경 성공 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("존재하지 않는 코스의 식별값으로 요청하면, http status 400 반환한다.")
        void notExistCourse() throws Exception {
            // given
            Long userId = 1L;
            Long courseId = 3L;

            order = 1;
            List<CoursePlaceSaveRequest> saveRequests = generateCoursePlaceSaveRequests(2);

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, null, null);

            Long currentUserId = userId;

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            willThrow(new EntityNotFoundException("해당 식별자를 가진 Course가 없습니다. 요청한 Course 식별값 : " + courseId))
                    .given(coursePlaceService)
                    .batchUpdateCoursePlace(eq(courseId), eq(currentUserId), anyList(), anyList(), anyList());

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.ENTITY_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.ENTITY_NOT_FOUND.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("코스를 작성한 유저가 아니면, http status 403 반환한다.")
        void notWriter() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            order = 1;
            List<CoursePlaceSaveRequest> saveRequests = generateCoursePlaceSaveRequests(2);

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, null, null);

            Long currentUserId = 3L;

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            willThrow(new CustomException("해당 코스에 장소를 등록 할 권한이 없습니다. 요청한 유저 식별값 : " + userId, ErrorCode.NO_AUTHORITIES))
                    .given(coursePlaceService)
                    .batchUpdateCoursePlace(eq(courseId), eq(currentUserId), anyList(), anyList(), anyList());

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.NO_AUTHORITIES.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.NO_AUTHORITIES.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("등록 데이터 검증에 실패하면, http status 400 반환한다.")
        void saveRequestValidFail() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            List<CoursePlaceSaveRequest> saveRequests = new ArrayList<>();
            saveRequests.add(new CoursePlaceSaveRequest());
            saveRequests.add(new CoursePlaceSaveRequest());

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, null, null);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("수정 데이터 검증에 실패하면, http status 400 반환한다.")
        void modifyRequestValidFail() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            setCoursePlaces(course, 2);
            Long courseId = course.getId();

            List<CoursePlaceModifyRequest> modifyRequests = new ArrayList<>();
            modifyRequests.add(new CoursePlaceModifyRequest());
            modifyRequests.add(new CoursePlaceModifyRequest());

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(null, modifyRequests, null);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("삭제 데이터 검증에 실패하면, http status 400 반환한다.")
        void deleteRequestValidFail() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            setCoursePlaces(course, 2);
            Long courseId = course.getId();

            List<CoursePlaceDeleteRequest> deleteRequests = new ArrayList<>();
            deleteRequests.add(new CoursePlaceDeleteRequest());
            deleteRequests.add(new CoursePlaceDeleteRequest());

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(null, null, deleteRequests);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터에서 중복된 코스 식별값이 존재하면 검증에 실패하고, http status 400 반환한다.")
        void duplicatedCourseId() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            List<CoursePlaceModifyRequest> modifyRequests = new ArrayList<>();
            for (int i = 1; i <= 2; i++) {
                CoursePlaceModifyRequest modifyRequest = new CoursePlaceModifyRequest();
                modifyRequest.setId((long) i);
                modifyRequest.setOrder(i);

                modifyRequests.add(modifyRequest);
            }
            List<CoursePlaceDeleteRequest> deleteRequests = new ArrayList<>();
            deleteRequests.add(new CoursePlaceDeleteRequest(1L)); // 수정 데이터와 coursePlaceId 중복

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(null, modifyRequests, deleteRequests);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("요청 데이터에서 중복된 장소 순서가 존재하면 검증에 실패하고, http status 400 반환한다.")
        void duplicatedOrder() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            List<CoursePlaceSaveRequest> saveRequests = new ArrayList<>();
            String name = "newName";
            String description = "newDescription";
            Long kakaoPlaceId = 12345L;
            CoursePlaceCategory placeCategory = CoursePlaceCategory.ETC;
            for (int i = 1; i <= 2; i++) {
                Double lat = (nextDouble() * (38 - 36 + 1) + 36);
                Double lng = (nextDouble() * (128 - 126 + 1) + 126);
                saveRequests.add(
                        new CoursePlaceSaveRequest(
                                name + i,
                                description + i,
                                lat,
                                lng,
                                "서울특별시 중구 세종대로 99-" + nextInt(300),
                                i,
                                kakaoPlaceId + i,
                                placeCategory.name()
                        )
                );
            }
            List<CoursePlaceModifyRequest> modifyRequests = new ArrayList<>();
            for (int i = 1; i <= 2; i++) { // saveRequests와 order 중복
                CoursePlaceModifyRequest modifyRequest = new CoursePlaceModifyRequest();
                modifyRequest.setId((long) i);
                modifyRequest.setOrder(i);

                modifyRequests.add(modifyRequest);
            }

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, modifyRequests, null);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("해당 코스에 연관된 기존 장소들의 식별값들이 " +
                "요청 데이터의 수정, 삭제 장소들의 식별값을 모두 포함하지 않으면 " +
                "다른 코스의 장소 식별값을 명시한 것이므로, " +
                "검증에 실패하고 http status 400 반환한다.")
        void requestHasPlacesOfOtherCourse() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            setCoursePlaces(course, 3);
            Long courseId = course.getId();

            // 변경 데이터 모두 명시
            List<CoursePlaceModifyRequest> modifyRequests = course.getCoursePlaces().stream()
                    .map(coursePlace -> {
                                CoursePlaceModifyRequest modifyRequest = new CoursePlaceModifyRequest();
                                modifyRequest.setId(coursePlace.getId());
                                modifyRequest.setOrder(coursePlace.getOrder());
                                return modifyRequest;
                            }
                    )
                    .collect(Collectors.toList());

            // 삭제 데이터에 다른 코스에 등록된 장소 식별값
            List<CoursePlaceDeleteRequest> deleteRequests = new ArrayList<>();
            deleteRequests.add(new CoursePlaceDeleteRequest(1000L));

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(null, modifyRequests, deleteRequests);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(coursePlaceQueryService.getCoursePlaceIds(courseId))
                    .willReturn(course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList()));

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("CoursePlaceService 에서 순서 중복 오류가 발생한 경우")
        void coursePlaceServiceErrorOrderDuplicated() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            setCoursePlaces(course, 5);

            // 추가할 데이터
            List<CoursePlaceSaveRequest> saveRequests = new ArrayList<>();
            saveRequests.add(
                    new CoursePlaceSaveRequest(
                            "newName",
                            "newDescription",
                            (nextDouble() * (38 - 36 + 1) + 36),
                            (nextDouble() * (128 - 126 + 1) + 126),
                            "서울특별시 중구 세종대로 99-" + nextInt(300),
                            2,
                            12345L,
                            CoursePlaceCategory.ETC.name()
                    )
            );

            // 수정할 데이터
            List<CoursePlaceModifyRequest> modifyRequests = new ArrayList<>();
            modifyRequests.add(new CoursePlaceModifyRequest(course.getCoursePlaces().get(3).getId(), "설명 변경", null, null));

            // 삭제할 데이터
            List<CoursePlaceDeleteRequest> deleteRequests = new ArrayList<>();
            deleteRequests.add(new CoursePlaceDeleteRequest(course.getCoursePlaces().get(4).getId()));

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, modifyRequests, deleteRequests);

            Long currentUserId = userId;

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(coursePlaceQueryService.getCoursePlaceIds(courseId))
                    .willReturn(course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList()));
            willThrow(new CustomException("장소의 순서가 중복되었습니다.", ErrorCode.PLACE_ORDER_DUPLICATE))
                    .given(coursePlaceService)
                    .batchUpdateCoursePlace(eq(courseId), eq(currentUserId), anyList(), anyList(), anyList());

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.PLACE_ORDER_DUPLICATE.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.PLACE_ORDER_DUPLICATE.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("CoursePlaceService 에서 순서가 1부터 시작하지 않는 오류가 발생한 경우")
        void coursePlaceServiceErrorOrderNotStartOne() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            setCoursePlaces(course, 5);

            // 추가할 데이터
            List<CoursePlaceSaveRequest> saveRequests = new ArrayList<>();
            saveRequests.add(
                    new CoursePlaceSaveRequest(
                            "newName",
                            "newDescription",
                            (nextDouble() * (38 - 36 + 1) + 36),
                            (nextDouble() * (128 - 126 + 1) + 126),
                            "서울특별시 중구 세종대로 99-" + nextInt(300),
                            6,
                            12345L,
                            CoursePlaceCategory.ETC.name()
                    )
            );

            // 수정할 데이터
            List<CoursePlaceModifyRequest> modifyRequests = new ArrayList<>();
            modifyRequests.add(new CoursePlaceModifyRequest(course.getCoursePlaces().get(3).getId(), "설명 변경", null, null));

            // 삭제할 데이터
            List<CoursePlaceDeleteRequest> deleteRequests = new ArrayList<>();
            deleteRequests.add(new CoursePlaceDeleteRequest(course.getCoursePlaces().get(0).getId()));

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, modifyRequests, deleteRequests);

            Long currentUserId = userId;

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(coursePlaceQueryService.getCoursePlaceIds(courseId))
                    .willReturn(course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList()));
            willThrow(new CustomException("장소의 순서가 1부터 시작하지 않습니다.", ErrorCode.PLACE_ORDER_NOT_START_ONE))
                    .given(coursePlaceService)
                    .batchUpdateCoursePlace(eq(courseId), eq(currentUserId), anyList(), anyList(), anyList());

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.PLACE_ORDER_NOT_START_ONE.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.PLACE_ORDER_NOT_START_ONE.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("CoursePlaceService 에서 순서가 연속된 값으로 증하가지 않는 오류가 발생한 경우")
        void coursePlaceServiceErrorOrderConsecutive() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            Long courseId = course.getId();

            setCoursePlaces(course, 5);

            // 추가할 데이터
            List<CoursePlaceSaveRequest> saveRequests = new ArrayList<>();
            saveRequests.add(
                    new CoursePlaceSaveRequest(
                            "newName",
                            "newDescription",
                            (nextDouble() * (38 - 36 + 1) + 36),
                            (nextDouble() * (128 - 126 + 1) + 126),
                            "서울특별시 중구 세종대로 99-" + nextInt(300),
                            7,
                            12345L,
                            CoursePlaceCategory.ETC.name()
                    )
            );

            // 수정할 데이터
            List<CoursePlaceModifyRequest> modifyRequests = new ArrayList<>();
            modifyRequests.add(new CoursePlaceModifyRequest(course.getCoursePlaces().get(3).getId(), "설명 변경", null, null));

            // 삭제할 데이터
            List<CoursePlaceDeleteRequest> deleteRequests = new ArrayList<>();
            deleteRequests.add(new CoursePlaceDeleteRequest(course.getCoursePlaces().get(4).getId()));

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(saveRequests, modifyRequests, deleteRequests);

            Long currentUserId = userId;

            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(coursePlaceQueryService.getCoursePlaceIds(courseId))
                    .willReturn(course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList()));
            willThrow(new CustomException("장소의 순서가 연속적인 값들이 아닙니다.", ErrorCode.PLACE_ORDER_NOT_CONSECUTIVE))
                    .given(coursePlaceService)
                    .batchUpdateCoursePlace(eq(courseId), eq(currentUserId), anyList(), anyList(), anyList());

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.PLACE_ORDER_NOT_CONSECUTIVE.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.PLACE_ORDER_NOT_CONSECUTIVE.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("API 오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("해당 코스에 장소 등록 없이, 기존 장소들을 모두 삭제하려는 요청은 http status 400 반환한다. " +
                "코스에는 하나 이상의 장소가 남아있어야 한다.")
        void noSaveAllDeleteError() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            setCoursePlaces(course, 3);
            Long courseId = course.getId();

            // 삭제할 데이터(모든 장소 삭제)
            List<CoursePlaceDeleteRequest> deleteRequests = course.getCoursePlaces().stream()
                    .map(coursePlace -> new CoursePlaceDeleteRequest(coursePlace.getId()))
                    .collect(Collectors.toList());

            CoursePlaceBatchUpdateRequest request = new CoursePlaceBatchUpdateRequest(null, null, deleteRequests);

            Long currentUserId = userId;
            String accessToken = generateUserAccessToken(currentUserId);

            // mocking
            given(coursePlaceQueryService.getCoursePlaceIds(courseId))
                    .willReturn(course.getCoursePlaces().stream().map(CoursePlace::getId).collect(Collectors.toList()));

            // when
            String path = "/courses/{courseId}/course-places/batch";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.post(path, courseId)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_TYPE + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .content(objectMapper.writeValueAsString(request))
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.VALIDATION_FAIL.getCode()))
                    .andExpect(jsonPath("$.data.message").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    subsectionWithPath("message").type(JsonFieldType.OBJECT).description("API 오류 메시지")
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("코스 장소 리스트 조회")
    class coursePlaceList {

        @Test
        @DisplayName("작성 완료된 코스의 장소 리스트를 조회하면, 해당 코스의 장소 리스트를 응답으로 반환한다.")
        void success() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();
            setCoursePlaces(course, 5);

            Long courseId = course.getId();

            // mocking
            given(coursePlaceQueryService.getCoursePlaces(courseId))
                    .willReturn(
                            ListResponse.toListResponse(
                                    course.getCoursePlaces().stream()
                                            .map(CoursePlaceDetails::new)
                                            .collect(Collectors.toList())
                            )
                    );

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
                    .andExpect(jsonPath("$.data.contents[*].id").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].name").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].description").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].lat").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].lng").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].order").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].apiId").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].category").isNotEmpty())
                    .andExpect(jsonPath("$.data.contents[*].address").exists());

            // docs
            perform.andDo(
                    restDocs.document(
                            pathParameters(
                                    attributes(key("title").value(path)),
                                    parameterWithName("courseId").description("장소 리스트를 조회할 코스의 식별값")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("count").type(JsonFieldType.NUMBER).description("조회한 코스 장소 리스트의 수"),
                                    subsectionWithPath("contents").type(JsonFieldType.ARRAY).description("코스 장소 리스트")
                            ),
                            responseFields(
                                    beneathPath("data.contents").withSubsectionId("contents"),
                                    attributes(key("title").value("응답 필드")),
                                    fieldWithPath("id").type(JsonFieldType.NUMBER).description("코스 장소 식별값"),
                                    fieldWithPath("name").type(JsonFieldType.STRING).description("장소 이름"),
                                    fieldWithPath("description").type(JsonFieldType.STRING).description("장소 설명"),
                                    fieldWithPath("lat").type(JsonFieldType.NUMBER).description("장소 위도"),
                                    fieldWithPath("lng").type(JsonFieldType.NUMBER).description("장소 경도"),
                                    fieldWithPath("order").type(JsonFieldType.NUMBER).description("장소 순서"),
                                    fieldWithPath("apiId").type(JsonFieldType.NUMBER).description("Kakao Map에서 장소의 식별값"),
                                    fieldWithPath("category").type(JsonFieldType.STRING).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.PLACE_CATEGORY)),
                                    fieldWithPath("address").type(JsonFieldType.STRING).description("장소의 주소")
                            )
                    )
            );
        }

        @Test
        @DisplayName("작성 완료되지 않은 코스의 장소 리스트를 조회하면, http status 400 반환한다.")
        void notCompleteCourse() throws Exception {
            // given
            Long userId = 1L;
            Course course = setCourses(userId, 1).stream().findFirst().orElseThrow();

            Long courseId = course.getId();

            // mocking
            given(coursePlaceQueryService.getCoursePlaces(courseId))
                    .willThrow(new CustomException("작성 완료되지 않은 코스입니다. 요청한 코스 식별값 : " + courseId, ErrorCode.CAN_NOT_ACCESS_RESOURCE));

            // when
            String path = "/courses/{courseId}/course-places";
            ResultActions perform = mockMvc.perform(
                    RestDocumentationRequestBuilders.get(path, courseId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.CAN_NOT_ACCESS_RESOURCE.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.CAN_NOT_ACCESS_RESOURCE.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("예외 응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지")
                            )
                    )
            );
        }

        @Test
        @DisplayName("존재하지 않는 코스의 장소 리스트를 조회하면, http status 400 반환한다.")
        void noCourseError() throws Exception {
            // given
            Long courseId = 100L;

            // mocking
            given(coursePlaceQueryService.getCoursePlaces(courseId))
                    .willThrow(new EntityNotFoundException("해당 식별값의 코스가 존재하지 않습니다. 요청한 코스 식별값 : " + courseId));

            // when
            String path = "/courses/{courseId}/course-places";
            ResultActions perform = mockMvc.perform(
                    get(path, courseId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
            );

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.ENTITY_NOT_FOUND.getCode()))
                    .andExpect(jsonPath("$.data.message").value(ErrorCode.ENTITY_NOT_FOUND.getMessage()));

            // docs
            perform.andDo(
                    restDocs.document(
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    attributes(key("title").value("예외 응답 필드")),
                                    fieldWithPath("errorCode").type(JsonFieldType.NUMBER).description(RestDocsUtil.generateLinkCode(RestDocsUtil.DocUrl.ERROR_CODE)),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지")
                            )
                    )
            );
        }
    }
}
