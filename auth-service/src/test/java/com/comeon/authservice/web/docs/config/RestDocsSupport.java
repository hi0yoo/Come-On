package com.comeon.authservice.web.docs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.config.BeanIds;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.ServletException;

@ActiveProfiles("test")
@AutoConfigureRestDocs
@Import(RestDocsConfig.class)
@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {

    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp(final WebApplicationContext context,
               final RestDocumentationContextProvider provider) throws ServletException {
        DelegatingFilterProxy delegateProxyFilter = new DelegatingFilterProxy();
        delegateProxyFilter.init(new MockFilterConfig(context.getServletContext(), BeanIds.SPRING_SECURITY_FILTER_CHAIN));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(MockMvcRestDocumentation.documentationConfiguration(provider))
                .alwaysDo(MockMvcResultHandlers.print())
                .alwaysDo(restDocs)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .addFilters(delegateProxyFilter)
                .build();
    }
}
