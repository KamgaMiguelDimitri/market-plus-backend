package ticamac.dev_complex.market_plus.infrastructure.config;

import ticamac.dev_complex.market_plus.infrastructure.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── Publiques ───────────────────────────────
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/products/**",
                                "/api/categories/**")
                        .permitAll()

                        // ── Webhooks — appelés par Stripe/MoMo sans token ──
                        .requestMatchers(
                                "/api/payments/webhooks/**")
                        .permitAll()

                        // ── Client connecté ─────────────────────────
                        .requestMatchers(
                                "/api/cart/**",
                                "/api/orders/**",
                                "/api/users/me/**",
                                "/api/payments/**")
                        .hasAnyRole("CUSTOMER", "ADMIN", "SUPERADMIN")

                        // ── Admin ───────────────────────────────────
                        .requestMatchers("/api/admin/**")
                        .hasAnyRole("ADMIN", "SUPERADMIN")

                        // ── Super-Admin ─────────────────────────────
                        .requestMatchers("/api/superadmin/**")
                        .hasRole("SUPERADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}