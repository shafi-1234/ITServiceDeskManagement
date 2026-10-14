package com.servicedesk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CustomOidcUserService oidcUserService,
                                    @Value("${app.frontend-url}") String frontendUrl) throws Exception {
        http
            .cors(Customizer.withDefaults())
            // The API is called cross-origin from the React app with CORS locked to the frontend origin.
            // Turn on CSRF tokens as well if you deploy the frontend on a different site.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/**").permitAll()          // registration
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            // Return 401 for API calls instead of redirecting to a login page
            .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), new AntPathRequestMatcher("/api/**")))
            // 1) Username + password (POST /api/login, form fields: username, password)
            .formLogin(f -> f
                .loginProcessingUrl("/api/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler((req, res, auth) -> res.setStatus(HttpStatus.OK.value()))
                .failureHandler((req, res, ex) -> res.setStatus(HttpStatus.UNAUTHORIZED.value()))
                .permitAll())
            // 2) Google OAuth 2.0 / OpenID Connect
            .oauth2Login(o -> o
                .userInfoEndpoint(u -> u.oidcUserService(oidcUserService))
                .defaultSuccessUrl(frontendUrl, true))
            .logout(l -> l
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/logout", HttpMethod.POST.name()))
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpStatus.OK.value()))
                .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-url}") String frontendUrl) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of(frontendUrl));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/api/**", c);
        return s;
    }
}
