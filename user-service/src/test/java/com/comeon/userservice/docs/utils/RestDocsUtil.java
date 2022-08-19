package com.comeon.userservice.docs.utils;

import com.comeon.userservice.docs.utils.snippets.CustomRequestFieldsSnippet;
import com.comeon.userservice.docs.utils.snippets.CustomResponseFieldsSnippet;
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

}
