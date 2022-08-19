package com.comeon.authservice.common.auth.config;

import com.comeon.authservice.common.auth.filter.JwtAuthenticationExceptionFilter;
import com.comeon.authservice.common.auth.filter.JwtAuthenticationFilter;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.common.jwt.JwtRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Order(100)
@RequiredArgsConstructor
public class LogoutSecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    public JwtAuthenticationExceptionFilter jwtAuthenticationExceptionFilter() {
        return new JwtAuthenticationExceptionFilter(objectMapper);
    }

    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, jwtRepository);
    }

    @Bean
    public SecurityFilterChain logoutSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .mvcMatcher("/auth/logout")
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .cors().configurationSource(corsConfigurationSource)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().permitAll() // TODO 모두 허용으로 되어있음. 수정
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationExceptionFilter(), jwtAuthenticationFilter().getClass());

        return http.build();
    }
}
