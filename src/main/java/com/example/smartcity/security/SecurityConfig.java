package com.example.smartcity.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // 🚨 IMPORTANT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll() // autoriser tout
                );

        return http.build();
    }
}
