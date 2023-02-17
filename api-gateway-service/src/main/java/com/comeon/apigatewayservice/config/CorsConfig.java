package com.comeon.apigatewayservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

//@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final Environment environment;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> allowMethods = environment.getProperty("cors.allow.method", List.class);
        List<String> allowHeaders = environment.getProperty("cors.allow.headers", List.class);
        List<String> exposedHeaders = environment.getProperty("cors.exposed.headers", List.class);

        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.addAllowedOriginPattern("https://*.comeoncalender.netlify.app:[*]");
        corsConfiguration.addAllowedOriginPattern("https://*.comeon.directory:[*]");
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(allowMethods);
        corsConfiguration.setAllowedHeaders(allowHeaders);
        corsConfiguration.setExposedHeaders(exposedHeaders);

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return configurationSource;
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        return new CorsWebFilter(corsConfigurationSource());
    }
}
