package project.yourNews.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import project.yourNews.common.jwt.filter.JwtAuthenticationFilter;
import project.yourNews.common.jwt.filter.JwtExceptionFilter;
import project.yourNews.common.utils.jwt.JwtUtil;
import project.yourNews.domains.member.domain.Role;
import project.yourNews.security.token.tokenBlackList.TokenBlackListService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final TokenBlackListService tokenBlackListService;

    private static final String[] PUBLIC_ENDPOINTS = {"/js/**", "/css/**", "/",
            "/v1/users/check-username", "/v1/news", "/v1/users/subscribe",
            "/v1/email/**", "/v1/users/check-nickname", "/v1/auth/**", "/unsubscribe",
            "/v1/*/posts", "/sns/notifications", "/*.html", "/adm/*.html"};
    private static final String[] ANONYMOUS_ENDPOINTS = {"/v1/users"};
    private static final String[] ADMIN_ENDPOINTS = {"/v1/admin/**"};

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .formLogin(AbstractHttpConfigurer::disable)

                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                .requestMatchers(HttpMethod.POST, ANONYMOUS_ENDPOINTS).anonymous()
                                .requestMatchers(ADMIN_ENDPOINTS).hasAnyRole(String.valueOf(Role.ADMIN))
                                .anyRequest().authenticated())

                .addFilterBefore(new JwtAuthenticationFilter(userDetailsService, jwtUtil, objectMapper, tokenBlackListService),
                        UsernamePasswordAuthenticationFilter.class)

                .addFilterBefore(new JwtExceptionFilter(objectMapper), JwtAuthenticationFilter.class);

        return http.build();
    }
}
