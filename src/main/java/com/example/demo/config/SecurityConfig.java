package com.example.demo.config;

import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.authentication.AuthenticationProvider; 
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.http.SessionCreationPolicy;
//  BCrypt NoOp
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, // ** 
                          CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            
            // SECURITY HEADERS 
            .headers(headers -> headers
                // захист від клікджекінгу (заборона від вбудовування в iframe)
                .frameOptions(frameOptions -> frameOptions.deny())
                // заборона визначення типу контенту браузером (MIME-sniffing)
                .contentTypeOptions(contentType -> {}) 
                // Content Security Policy script-src 'self'
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                // defend against XSS attacks
               .xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/login", "/api/users", "/api/refreshtoken", "/api/logout").permitAll()
                .requestMatchers("/api/hello").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // (BCrypt strength 12)
    @Bean
    public PasswordEncoder passwordEncoder() {
        
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
            http.getSharedObject(AuthenticationManagerBuilder.class);
        
        authenticationManagerBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
            
        return authenticationManagerBuilder.build();
    }
}