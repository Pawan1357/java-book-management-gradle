package com.exam.library_management.config;

import com.exam.library_management.security.CustomUserDetailsService;
import com.exam.library_management.security.JwtAuthenticationFilter;
import com.exam.library_management.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {

        http
            // .authenticationProvider(authProvider)
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->{
                    // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("""
                        {"success":false,"message":"Invalid or expired JWT","data":null}
                    """);}
                )
                .accessDeniedHandler((request, response, accessDeniedException) ->{
                    // response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("""
                        {"success":false,"message":"Forbidden","data":null}
                    """);}
                )
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/user/**").hasRole("USER")
                    .anyRequest().authenticated()
            )
            // .authorizeHttpRequests(auth -> auth
            //     .requestMatchers("/api/auth/**").permitAll()
            //     .anyRequest().authenticated()
            // )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // @Bean
    // public DaoAuthenticationProvider authenticationProvider(
    //         CustomUserDetailsService userDetailsService,
    //         PasswordEncoder passwordEncoder) {

    //     DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    //     provider.setUserDetailsService(userDetailsService);
    //     provider.setPasswordEncoder(passwordEncoder);
    //     return provider;
    // }
    // @PostConstruct
    // public void loadEnv() {
    //     Dotenv dotenv = Dotenv.configure()
    //             .ignoreIfMissing()
    //             .load();

    //     dotenv.entries().forEach(entry -> {
    //         System.setProperty(entry.getKey(), entry.getValue());
    //     });
    // }

}
