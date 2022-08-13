package com.comeon.authservice.config;

import com.comeon.authservice.auth.filter.JwtAuthenticationExceptionFilter;
import com.comeon.authservice.auth.filter.ReissueAuthenticationFilter;
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
    private final JwtAuthenticationExceptionFilter jwtAuthenticationExceptionFilter;
    private final ReissueAuthenticationFilter reissueAuthenticationFilter;

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
                .anyRequest().permitAll();

        http.addFilterBefore(reissueAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationExceptionFilter, reissueAuthenticationFilter.getClass());

        return http.build();
    }

}
