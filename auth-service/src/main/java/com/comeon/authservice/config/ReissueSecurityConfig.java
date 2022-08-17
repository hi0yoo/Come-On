package com.comeon.authservice.config;

import com.comeon.authservice.auth.filter.ReissueAuthenticationExceptionFilter;
import com.comeon.authservice.auth.filter.ReissueAuthenticationFilter;
import com.comeon.authservice.auth.jwt.JwtTokenProvider;
import com.comeon.authservice.auth.jwt.JwtRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Order(200)
@RequiredArgsConstructor
public class ReissueSecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    public ReissueAuthenticationExceptionFilter reissueAuthenticationExceptionFilter() {
        return new ReissueAuthenticationExceptionFilter(objectMapper);
    }

    public ReissueAuthenticationFilter reissueAuthenticationFilter() {
        return new ReissueAuthenticationFilter(jwtTokenProvider, jwtRepository);
    }

    @Bean
    public SecurityFilterChain reissueSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .mvcMatcher("/auth/reissue")
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .cors().configurationSource(corsConfigurationSource)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(reissueAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(reissueAuthenticationExceptionFilter(), reissueAuthenticationFilter().getClass());

        return http.build();
    }

}
