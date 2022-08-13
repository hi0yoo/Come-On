package com.comeon.authservice.config;

import com.comeon.authservice.auth.filter.JwtAuthenticationExceptionFilter;
import com.comeon.authservice.auth.filter.JwtAuthenticationFilter;
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
    private final JwtAuthenticationExceptionFilter jwtAuthenticationExceptionFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

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
                .anyRequest().authenticated();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationExceptionFilter, jwtAuthenticationFilter.getClass());

        return http.build();
    }
}
