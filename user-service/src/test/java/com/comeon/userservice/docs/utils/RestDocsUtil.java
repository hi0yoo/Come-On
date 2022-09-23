package com.comeon.userservice.docs.utils;

import com.comeon.userservice.docs.utils.snippets.CustomResponseFieldsSnippet;
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
        OAUTH_PROVIDER("enums", "oauth-provider", "소셜 로그인 서비스 제공자 코드"),
        USER_STATUS("enums", "user-status", "유저 상태 코드"),
        USER_ROLE("enums", "user-role", "유저 권한 코드"),
        ERROR_CODE("common", "error-codes", "오류 응답 코드"),
        ;

        @Getter
        private final String dirName;

        @Getter
        private final String pageId;

        @Getter
        private final String text;
    }

}
