package com.comeon.userservice.docs.utils;

import com.comeon.userservice.docs.utils.snippets.CustomRequestFieldsSnippet;
import com.comeon.userservice.docs.utils.snippets.CustomResponseFieldsSnippet;
import com.comeon.userservice.docs.utils.snippets.OptionalRequestHeaderSnippet;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;

import java.util.Arrays;
import java.util.Map;

public class RestDocsUtil {

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

    public static OptionalRequestHeaderSnippet optionalRequestHeaders(
            String type,
            Map<String, Object> attributes,
            HeaderDescriptor... descriptors) {

        return new OptionalRequestHeaderSnippet(type, Arrays.asList(descriptors), attributes);
    }

}
