package com.comeon.apigatewayservice.docs.utils;

import com.comeon.apigatewayservice.docs.utils.snippets.CustomResponseFieldsSnippet;
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
