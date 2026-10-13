package com.example.kambaapijava.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.time.Instant;
import java.util.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, com.example.kambaapijava.security.JwtFilter jwt, ObjectMapper mapper) throws Exception {
        return http.csrf(c -> c.disable()).cors(c -> {
        }).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(e -> e.authenticationEntryPoint((r, s, x) -> {
            s.setStatus(401);
            s.setContentType("application/json");
            mapper.writeValue(s.getOutputStream(), Map.of("timestamp", Instant.now(), "status", 401, "error", "Unauthorized", "message", "Autenticación requerida", "path", r.getRequestURI(), "details", List.of()));
        }).accessDeniedHandler((r, s, x) -> {
            s.setStatus(403);
            s.setContentType("application/json");
            mapper.writeValue(s.getOutputStream(), Map.of("timestamp", Instant.now(), "status", 403, "error", "Forbidden", "message", "Acceso denegado", "path", r.getRequestURI(), "details", List.of()));
        })).authorizeHttpRequests(a -> a.requestMatchers("/api/v1/auth/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll().anyRequest().authenticated()).addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class).build();
    }

    @Bean
    CorsConfigurationSource cors(@org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins}") String origins) {
        var c = new CorsConfiguration();
        c.setAllowedOrigins(Arrays.asList(origins.split(",")));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        var s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}
