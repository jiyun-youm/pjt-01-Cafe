package com.miniproject.cafe.Config;

import com.miniproject.cafe.Handler.FormLoginFailureHandler;
import com.miniproject.cafe.Handler.OAuth2FailureHandler;
import com.miniproject.cafe.Handler.FormLoginSuccessHandler;
import com.miniproject.cafe.Handler.OAuthLoginSuccessHandler;
import com.miniproject.cafe.Service.CustomOAuth2UserService;
import com.miniproject.cafe.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailsService customUserDetailsService;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final FormLoginFailureHandler formLoginFailureHandler;
    private final FormLoginSuccessHandler formLoginSuccessHandler;
    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CSRF 및 iframe 허용
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))

                // URL 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home/", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/api/member/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()

                        // 로그인 필요 메뉴
                        .requestMatchers("/home/order_history").authenticated()
                        .requestMatchers("/home/mypick").authenticated()
                        .requestMatchers("/home/cart").authenticated()
                        .requestMatchers("/account/**").authenticated()

                        // 메뉴는 누구나 접근 가능
                        .requestMatchers("/menu/**").permitAll()

                        // 나머지 모두 허용
                        .anyRequest().permitAll()
                )

                // 일반 로그인(formLogin)
                .formLogin(f -> f
                        .loginPage("/home/")
                        .loginProcessingUrl("/login")
                        .successHandler(formLoginSuccessHandler)
                        .failureHandler(formLoginFailureHandler)
                        .permitAll()
                )

                // OAuth2 소셜 로그인
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/home/")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuthLoginSuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )

                // 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )

                // 자동 로그인(Remember Me)
                .rememberMe(r -> r
                        .key("secure-key")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(60 * 60 * 24 * 14)
                        .userDetailsService(customUserDetailsService)
                );

        return http.build();
    }
}
