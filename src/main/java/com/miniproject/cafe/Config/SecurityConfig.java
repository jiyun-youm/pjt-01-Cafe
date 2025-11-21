package com.miniproject.cafe.Config;

import com.miniproject.cafe.Filter.SessionSetupFilter;
import com.miniproject.cafe.Handler.FormLoginFailureHandler;
import com.miniproject.cafe.Handler.FormLoginSuccessHandler;
import com.miniproject.cafe.Handler.OAuth2FailureHandler;
import com.miniproject.cafe.Handler.OAuthLoginSuccessHandler;
import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.Service.AdminUserDetailsService;
import com.miniproject.cafe.Service.CustomOAuth2UserService;
import com.miniproject.cafe.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AdminUserDetailsService adminUserDetailsService;

    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final FormLoginFailureHandler formLoginFailureHandler;

    private final MemberMapper memberMapper;
    private final AdminMapper adminMapper;

    private static final String REMEMBER_ME_KEY = "secure-key";

    // ... (RememberMe Services Bean 설정은 기존과 동일하게 유지) ...

    @Bean
    public RememberMeServices rememberMeServices() {
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
                REMEMBER_ME_KEY,
                customUserDetailsService
        );
        services.setAlwaysRemember(false);
        services.setTokenValiditySeconds(60 * 60 * 24 * 14);
        services.setCookieName("remember-me");
        services.setParameter("remember-me");
        return services;
    }

    @Bean
    public RememberMeServices oauthRememberMeServices() {
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
                REMEMBER_ME_KEY,
                customUserDetailsService
        );
        services.setAlwaysRemember(true);
        services.setTokenValiditySeconds(60 * 60 * 24 * 14);
        services.setCookieName("remember-me");
        return services;
    }

    @Bean
    public RememberMeServices adminRememberMeServices() {
        TokenBasedRememberMeServices services = new TokenBasedRememberMeServices(
                REMEMBER_ME_KEY,
                adminUserDetailsService
        );
        services.setAlwaysRemember(false);
        services.setTokenValiditySeconds(60 * 60 * 24 * 14);
        services.setCookieName("remember-me-admin");
        services.setParameter("remember-me");
        return services;
    }

    @Bean
    public OAuthLoginSuccessHandler oAuthLoginSuccessHandler() {
        return new OAuthLoginSuccessHandler(memberMapper, oauthRememberMeServices());
    }

    @Bean
    public FormLoginSuccessHandler formLoginSuccessHandler() {
        return new FormLoginSuccessHandler(memberMapper, rememberMeServices());
    }

    // ============================================================
    // [관리자용 필터 체인]
    // ============================================================
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/signup", "/admin/joinForm", "/admin/checkId", "/admin/css/**", "/admin/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/perform_login_process")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me-admin")
                )
                .rememberMe(r -> r
                        .rememberMeServices(adminRememberMeServices())
                )
                // ★★★ [수정 포인트] ★★★
                // RememberMe 필터가 실행된 '후'에 세션 복구 필터를 실행해야 합니다.
                .addFilterAfter(new SessionSetupFilter(memberMapper, adminMapper), RememberMeAuthenticationFilter.class);

        return http.build();
    }

    // ============================================================
    // [사용자용 필터 체인]
    // ============================================================
    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/upload/**").permitAll()
                        .requestMatchers("/api/member/**", "/oauth2/**", "/login").permitAll()
                        .requestMatchers("/menu/**").permitAll()
                        .requestMatchers("/home/saveRegion", "/home/getRegion").permitAll()
                        .requestMatchers("/home/login").permitAll()
                        .requestMatchers("/home/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(f -> f
                        .loginPage("/home/login")
                        .loginProcessingUrl("/login")
                        .successHandler(formLoginSuccessHandler())
                        .failureHandler(formLoginFailureHandler)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/home/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuthLoginSuccessHandler())
                        .failureHandler(oAuth2FailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home/login")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                )
                .rememberMe(r -> r
                        .rememberMeServices(rememberMeServices())
                )
                // ★★★ [수정 포인트] 사용자 체인도 동일하게 수정 ★★★
                .addFilterAfter(new SessionSetupFilter(memberMapper, adminMapper), RememberMeAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}