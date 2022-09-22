package com.comeon.authservice.config.security;

import com.comeon.authservice.config.security.filter.JwtAuthenticationExceptionFilter;
import com.comeon.authservice.config.security.filter.JwtAuthenticationFilter;
import com.comeon.authservice.common.jwt.JwtTokenProvider;
import com.comeon.authservice.common.jwt.RedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Order(100)
@RequiredArgsConstructor
public class JwtSecurityConfig {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRepository jwtRepository;

    public JwtAuthenticationExceptionFilter jwtAuthenticationExceptionFilter() {
        return new JwtAuthenticationExceptionFilter(objectMapper);
    }

    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, jwtRepository);
    }

    @Bean
    public SecurityFilterChain logoutSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .mvcMatcher("/auth/**")
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/auth/logout", "/auth/validate").authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationExceptionFilter(), jwtAuthenticationFilter().getClass());

        return http.build();
    }
}
