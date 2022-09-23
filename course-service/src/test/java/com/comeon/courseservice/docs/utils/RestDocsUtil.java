package com.comeon.courseservice.docs.utils;

import com.comeon.courseservice.docs.utils.snippets.CustomResponseFieldsSnippet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;

import java.util.Arrays;
import java.util.Map;

public class RestDocsUtil {

    public static CustomResponseFieldsSnippet customResponseFields(
            String type, PayloadSubsectionExtractor<?> subsectionExtractor,
            Map<String, Object> attributes, FieldDescriptor... descriptors) {

        return new CustomResponseFieldsSnippet(
                type, subsectionExtractor,
                Arrays.asList(descriptors),
                attributes, true
        );
    }

    public static String generateLinkCode(DocUrl docUrl) {
        return String.format("link:%s/%s.html[%s,role=\"popup\"]", docUrl.getDirName(), docUrl.getPageId(), docUrl.getText());
    }

    public static String generateText(DocUrl docUrl) {
        return String.format("%s %s", docUrl.text, "코드명");
    }

    @RequiredArgsConstructor
    public enum DocUrl {
        PLACE_CATEGORY("enums", "place-category", "장소 카테고리 코드"),
        COURSE_STATUS("enums", "course-status", "코스 작성 상태 코드"),
        ERROR_CODE("common", "error-code", "예외 응답 코드"),
        ;

        @Getter
        private final String dirName;

        @Getter
        private final String pageId;

        @Getter
        private final String text;
    }

}
