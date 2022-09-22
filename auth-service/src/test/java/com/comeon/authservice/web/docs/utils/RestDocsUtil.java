package com.comeon.authservice.web.docs.utils;

import com.comeon.authservice.web.docs.utils.snippets.CustomRequestFieldsSnippet;
import com.comeon.authservice.web.docs.utils.snippets.CustomRequestHeadersSnippet;
import com.comeon.authservice.web.docs.utils.snippets.CustomResponseFieldsSnippet;
import com.comeon.authservice.web.docs.utils.snippets.CustomResponseHeadersSnippet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;

import java.util.Arrays;
import java.util.Map;

public class RestDocsUtil {

    public static CustomRequestHeadersSnippet customRequestHeaders(
            String type,
            Map<String, Object> attributes,
            HeaderDescriptor... descriptors) {

        return new CustomRequestHeadersSnippet(type, Arrays.asList(descriptors), attributes);
    }

    public static CustomResponseHeadersSnippet customResponseHeaders(
            String type,
            Map<String, Object> attributes,
            HeaderDescriptor... descriptors) {

        return new CustomResponseHeadersSnippet(type, Arrays.asList(descriptors), attributes);
    }

    public static CustomRequestFieldsSnippet customRequestFields(
            String type, PayloadSubsectionExtractor<?> subsectionExtractor,
            Map<String, Object> attributes, FieldDescriptor... descriptors) {

        return new CustomRequestFieldsSnippet(
                type, subsectionExtractor, Arrays.asList(descriptors),
                attributes, true
        );
    }

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
        ERROR_CODE("common", "error-code", "API 오류 응답 코드"),
        OAUTH_PROVIDER_CODE("enum", "oauth-provider-code", "소셜 로그인 서비스 제공 벤더 코드")
        ;

        @Getter
        private final String dirName;

        @Getter
        private final String pageId;

        @Getter
        private final String text;
    }
}
