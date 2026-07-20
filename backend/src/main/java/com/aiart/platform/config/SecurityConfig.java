package com.aiart.platform.config;

import com.aiart.platform.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/public/**", "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tags/**", "/api/artworks/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/artworks/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tags/build-prompt").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/generation/provider/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/style-packages/market").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/style-packages/*/artworks").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tasks/market").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
