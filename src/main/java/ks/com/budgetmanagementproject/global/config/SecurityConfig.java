package ks.com.budgetmanagementproject.global.config;

import ks.com.budgetmanagementproject.feature.role.repository.RoleRepository;
import ks.com.budgetmanagementproject.feature.token.service.RedisBlackTokenService;
import ks.com.budgetmanagementproject.feature.token.service.RedisRefreshTokenService;
import ks.com.budgetmanagementproject.global.jwt.JWTFilter;
import ks.com.budgetmanagementproject.global.jwt.JWTUtil;
import ks.com.budgetmanagementproject.global.jwt.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final RedisRefreshTokenService redisRefreshTokenService;
    private final RedisBlackTokenService redisBlackTokenService;

    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    private static final String[] ALL_ALLOW_PAGE = {
             "/", "/chat.html", "/favicon.ico",
             "/static/**", "/js/**",
             "/ws-stomp", "/ws-stomp/**", "/pub/**", "/sub/**",
             "/users/**", "/reissue", "/error", "/chat", "/chat/**", "/rooms/**"
    };

    private static final String[] SWAGGER_ALLOW_PAGE = {
             "/swagger", "/swagger-ui.html", "/swagger-ui/**",
             "/api-docs", "/api-docs/**", "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests((auth -> auth
                        .requestMatchers(ALL_ALLOW_PAGE).permitAll()
                        .requestMatchers(SWAGGER_ALLOW_PAGE).permitAll()
                        .anyRequest().authenticated())
                )
                .addFilterBefore(new JWTFilter(jwtUtil, roleRepository, redisRefreshTokenService, redisBlackTokenService), LoginFilter.class)
                .addFilterAt(new LoginFilter(authenticationManager, jwtUtil, redisRefreshTokenService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
