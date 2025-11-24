package com.miniproject.cafe.Config;

import com.miniproject.cafe.Filter.SessionSetupFilter;
import com.miniproject.cafe.Handler.*;
import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.Service.AdminUserDetailsService;
import com.miniproject.cafe.Service.CustomOAuth2UserService;
import com.miniproject.cafe.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

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

    private final RememberMeSuccessHandler rememberMeSuccessHandler;

    private static final String REMEMBER_KEY = "secure-key";

    /* ==========================
        Authentication Providers
    =========================== */
    @Bean
    public AuthenticationProvider userProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider adminProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationProvider oauth2Provider() {
        return new OAuth2LoginAuthenticationProvider(
                new DefaultAuthorizationCodeTokenResponseClient(),
                customOAuth2UserService
        );
    }

    @Bean
    public AuthenticationProvider oidcProvider() {
        OAuth2UserService<OidcUserRequest, OidcUser> oidc = new OidcUserService();
        return new OidcAuthorizationCodeAuthenticationProvider(
                new DefaultAuthorizationCodeTokenResponseClient(),
                oidc
        );
    }

    /* ======================
         Remember-Me 설정
    ======================= */
    @Bean
    public RememberMeServices memberRememberMe() {
        TokenBasedRememberMeServices s =
                new TokenBasedRememberMeServices(REMEMBER_KEY, customUserDetailsService);
        s.setTokenValiditySeconds(60 * 60 * 24 * 14);
        return s;
    }

    @Bean
    public RememberMeServices oauthRememberMe() {
        TokenBasedRememberMeServices s =
                new TokenBasedRememberMeServices(REMEMBER_KEY, customUserDetailsService);
        s.setAlwaysRemember(true);
        s.setTokenValiditySeconds(60 * 60 * 24 * 14);
        return s;
    }

    /* ======================
        관리자 Security
    ======================= */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/admin/**")
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(adminProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/admin/login",
                                "/admin/signup",
                                "/admin/joinForm",
                                "/admin/checkId",
                                "/admin/css/**",
                                "/admin/js/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(f -> f
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/perform_login_process")
                        .usernameParameter("id")
                        .passwordParameter("pw")
                        .defaultSuccessUrl("/admin/orders", false)
                        .failureHandler(formLoginFailureHandler)
                )
                .logout(l -> l
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me-admin")
                )
                .rememberMe(r -> r
                        .key("admin-secure-key")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(60 * 60 * 24 * 14)
                        .userDetailsService(adminUserDetailsService)
                        .rememberMeCookieName("remember-me-admin")
                )
                .addFilterAfter(new SessionSetupFilter(memberMapper, adminMapper),
                        RememberMeAuthenticationFilter.class);

        return http.build();
    }

    /* ======================
        사용자 Security
    ======================= */
    @Bean
    @Order(2)
    public SecurityFilterChain userChain(HttpSecurity http) throws Exception {

        http
                .authenticationProvider(userProvider())
                .authenticationProvider(oauth2Provider())
                .authenticationProvider(oidcProvider())
                .csrf(csrf -> csrf.disable())

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
                        .successHandler(new FormLoginSuccessHandler(memberMapper, memberRememberMe()))
                        .failureHandler(formLoginFailureHandler)
                )

                .oauth2Login(o -> o
                        .loginPage("/home/login")
                        .userInfoEndpoint(e -> e.userService(customOAuth2UserService))
                        .successHandler(new OAuthLoginSuccessHandler(memberMapper, oauthRememberMe()))
                        .failureHandler(oAuth2FailureHandler)
                )

                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home/login")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .invalidateHttpSession(true)
                )

                .rememberMe(r -> r
                        .rememberMeServices(memberRememberMe())
                        .authenticationSuccessHandler(rememberMeSuccessHandler)
                )

                .addFilterAfter(new SessionSetupFilter(memberMapper, adminMapper),
                        RememberMeAuthenticationFilter.class);

        return http.build();
    }

    /* =======================
        패스워드 인코더
    ======================= */
    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
