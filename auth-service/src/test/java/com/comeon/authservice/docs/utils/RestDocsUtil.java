package com.comeon.authservice.docs.utils;

import com.comeon.authservice.docs.utils.snippets.*;
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
}
