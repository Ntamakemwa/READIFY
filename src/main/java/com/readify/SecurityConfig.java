package com.readify;

import com.readify.security.CustomUserDetailsService;
import com.readify.security.JwtAuthenticationFilter;
import com.readify.security.jwt.JwtTokenProvider;
import com.readify.oauth2.CustomOAuth2UserService;
import com.readify.oauth2.OAuth2AuthenticationSuccessHandler;
import com.readify.oauth2.OAuth2AuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtTokenProvider tokenProvider,
                                                   CustomUserDetailsService userDetailsService,
                                                   CustomOAuth2UserService customOAuth2UserService,
                                                   OAuth2AuthenticationSuccessHandler oauth2SuccessHandler,
                                                   OAuth2AuthenticationFailureHandler oauth2FailureHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .httpBasic(httpBasic -> httpBasic.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/h2-console/**").permitAll()
                        // React app: page shells and built assets
                        .requestMatchers(
                                "/index.html", "/assets/**", "/*.svg", "/favicon.ico",
                                "/login", "/catalog", "/books/**", "/my-loans", "/admin"
                        ).permitAll()
                        // Other static assets
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                        // Auth endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register").permitAll()
                        // Public API endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/books/**", "/api/v1/authors/**", "/api/v1/categories/**").permitAll()
                        // OAuth2
                        .requestMatchers("/api/v1/auth/oauth2/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        // Swagger
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService))
                .successHandler(oauth2SuccessHandler)
        );

        http.addFilterBefore(new JwtAuthenticationFilter(tokenProvider, userDetailsService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}