package com.jj.market.config;

import com.jj.market.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.jj.market.service.CustomOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // 이 부분 추가
@Slf4j
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(
            UserService userService,
            PasswordEncoder passwordEncoder,
            CustomOAuth2UserService customOAuth2UserService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/fonts/**",
                    "/assets/**",
                    "/index/**",
                    "/shop/**",
                    "/login/**",
                    "/register/**",
                    "/error"
                ).permitAll()
                .requestMatchers("/productRegister/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("userID")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    // 세션에 명시적으로 인증 정보 저장
                    HttpSession session = request.getSession(true);
                    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    
                    log.info("Login Success. Authentication: {}", authentication);
                    log.info("Principal: {}", authentication.getPrincipal());
                    response.sendRedirect("/");
                })
                .failureHandler((request, response, exception) -> {
                    log.error("Login Failed: {}", exception.getMessage());
                    response.sendRedirect("/login?error=true");
                })
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .addLogoutHandler((request, response, authentication) -> {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    if (authentication != null && 
                        authentication.getPrincipal() instanceof OAuth2User) {
                        String kakaoLogout = "https://kauth.kakao.com/oauth/logout"
                                + "?client_id=4ef903792704ba3070a9f6b491f7beb7"
                                + "&logout_redirect_uri=http://localhost:8080/";
                        try {
                            response.sendRedirect(kakaoLogout);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            response.sendRedirect("/");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // 나머지 설정 유지...
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired")
                .sessionRegistry(sessionRegistry()) // 추가
            );

        return http.build();
    }

    // 세션 관리를 위한 빈 추가
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}