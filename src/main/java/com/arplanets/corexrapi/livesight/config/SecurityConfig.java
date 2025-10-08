package com.arplanets.corexrapi.livesight.config;

import com.arplanets.corexrapi.livesight.log.filter.InitRequestContextFilter;
import com.arplanets.corexrapi.livesight.log.filter.LoggingFilter;
import com.arplanets.corexrapi.livesight.security.handler.CustomAuthenticationEntrypoint;
import com.arplanets.corexrapi.livesight.security.handler.CustomAccessDeniedHandler;
import com.arplanets.corexrapi.livesight.security.jwt.MultiIssuerJwtDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity
public class SecurityConfig {

    private final InitRequestContextFilter initRequestContextFilter;
    private final LoggingFilter loggingFilter;
    private final CustomAuthenticationEntrypoint customAuthenticationEntrypoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Value("${order.access-token.trusted-issuers}")
    private List<String> trustedIssuers;

    @Value("${server.servlet.custom.context-path:}")
    private String contextPath;



    @Bean
    @Order(1)
    @Profile({"dev", "test"})
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        http
        .securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
        .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain rootFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain publicApiWithoutAuditFilterChain(HttpSecurity http) throws Exception {
        http
        .securityMatcher(contextPath + "/api/order/fetch_status", contextPath + "/.well-known/jwks.json", contextPath + "/api/auth/**")
        .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(initRequestContextFilter, WebAsyncManagerIntegrationFilter.class);

        return http.build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain mgApiWithoutAuditFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
        .cors(withDefaults())
        .securityMatcher(contextPath + "/mg/api/order/info", contextPath + "/mg/api/order/list", contextPath + "/mg/api/live-sight/**")
        .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder)))
        .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(customAuthenticationEntrypoint)
                .accessDeniedHandler(customAccessDeniedHandler))
        .addFilterBefore(initRequestContextFilter, WebAsyncManagerIntegrationFilter.class);

        return http.build();
    }

    @Bean
    @Order(5)
    public SecurityFilterChain publicApiFilterChain(HttpSecurity http) throws Exception {
        http
        .securityMatcher(contextPath + "/api/order/**")
        .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(loggingFilter, WebAsyncManagerIntegrationFilter.class)
        .addFilterBefore(initRequestContextFilter, LoggingFilter.class);

        return http.build();
    }

    @Bean
    @Order(6)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
        .cors(withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder)))
        .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(customAuthenticationEntrypoint)
                .accessDeniedHandler(customAccessDeniedHandler))
        .addFilterBefore(loggingFilter, WebAsyncManagerIntegrationFilter.class)
        .addFilterBefore(initRequestContextFilter, LoggingFilter.class);

        return http.build();
    }


    @Bean
    public JwtDecoder jwtDecoder() {
        return new MultiIssuerJwtDecoder(trustedIssuers);
    }


}

