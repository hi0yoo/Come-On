package com.comeon.authservice.config.security;

import com.comeon.authservice.config.security.filter.LogoutExceptionFilter;
import com.comeon.authservice.config.security.handler.OAuth2LogoutHandler;
import com.comeon.authservice.config.security.oauth.handler.CustomOAuth2AuthenticationFailureHandler;
import com.comeon.authservice.config.security.oauth.handler.CustomOAuth2AuthenticationSuccessHandler;
import com.comeon.authservice.config.security.oauth.repository.CustomAuthorizationRequestRepository;
import com.comeon.authservice.config.security.oauth.service.CustomOAuth2UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Order(300)
@Configuration
@EnableWebSecurity
@Import({
        ReissueSecurityConfig.class,
        JwtSecurityConfig.class
})
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final OAuth2LogoutHandler oAuth2LogoutHandler;
    private final CustomAuthorizationRequestRepository authorizationRequestRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final CustomOAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    public LogoutExceptionFilter logoutExceptionFilter() {
        return new LogoutExceptionFilter(objectMapper);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring().antMatchers("/docs/**");
        };
    }

    @Bean
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .mvcMatcher("/oauth2/**")
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .anyRequest().permitAll()

                .and()
                .logout()
                .logoutUrl("/oauth2/logout")
                .addLogoutHandler(oAuth2LogoutHandler)

                .and()
                .addFilterBefore(logoutExceptionFilter(), LogoutFilter.class)

                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/oauth2/authorize/**")
                .authorizationRequestRepository(authorizationRequestRepository)

                .and()
                .redirectionEndpoint()
                .baseUri("/oauth2/callback/*")

                .and()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)

                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler);

        return http.build();
    }

}