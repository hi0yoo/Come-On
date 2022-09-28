package com.comeon.authservice.web.docs.utils.snippets;

import org.springframework.restdocs.headers.AbstractHeadersSnippet;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.operation.Operation;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CustomRequestHeadersSnippet extends AbstractHeadersSnippet {

    public CustomRequestHeadersSnippet(String type, List<HeaderDescriptor> descriptors, Map<String, Object> attributes) {
        super(type, descriptors, attributes);
    }

    @Override
    protected Set<String> extractActualHeaders(Operation operation) {
        return operation.getRequest().getHeaders().keySet();
    }
}
