package com.miniproject.cafe.Config;

import com.miniproject.cafe.Filter.AdminSessionSetupFilter;
import com.miniproject.cafe.Filter.UserSessionSetupFilter;
import com.miniproject.cafe.Handler.*;
import com.miniproject.cafe.Mapper.AdminMapper;
import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.Repository.AdminSecurityContextRepository;
import com.miniproject.cafe.Repository.UserSecurityContextRepository;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AdminUserDetailsService adminUserDetailsService;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final FormLoginFailureHandlerForAdmin formLoginFailureHandlerForAdmin;
    private final FormLoginFailureHandlerForUser formLoginFailureHandlerForUser;

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
    public RememberMeServices adminRememberMe() {
        TokenBasedRememberMeServices s =
                new TokenBasedRememberMeServices("admin-secure-key", adminUserDetailsService);
        s.setTokenValiditySeconds(60 * 60 * 24 * 14);
        s.setParameter("remember-me-admin");
        s.setCookieName("remember-me-admin");
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
        SecurityContextRepository Bean
    ======================= */
    @Bean
    public AdminSecurityContextRepository adminSecurityContextRepository() {
        return new AdminSecurityContextRepository();
    }

    @Bean
    public UserSecurityContextRepository userSecurityContextRepository() {
        return new UserSecurityContextRepository();
    }

    /* ======================
        관리자 Security
    ======================= */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http,
                                                AdminSecurityContextRepository adminSecRepo) throws Exception {

        http
                .securityMatcher("/admin/**")
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(adminProvider())

                .securityContext(sc -> sc
                        .securityContextRepository(adminSecRepo)
                )

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
                        .successHandler(new AdminLoginSuccessHandler(adminMapper, adminRememberMe()))
                        .failureHandler(formLoginFailureHandlerForAdmin)
                )

                .logout(l -> l
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login")
                        .deleteCookies("remember-me-admin")
                )

                .rememberMe(r -> r
                        .rememberMeServices(adminRememberMe())
                        .rememberMeParameter("remember-me-admin")
                        .key("admin-secure-key")
                )

                .addFilterAfter(new AdminSessionSetupFilter(adminMapper),
                        RememberMeAuthenticationFilter.class);

        return http.build();
    }

    /* ======================
        사용자 Security
    ======================= */
    @Bean
    @Order(2)
    public SecurityFilterChain userChain(HttpSecurity http,
                                         UserSecurityContextRepository userSecRepo) throws Exception {

        http
                .authenticationProvider(userProvider())
                .authenticationProvider(oauth2Provider())
                .authenticationProvider(oidcProvider())
                .csrf(csrf -> csrf.disable())

                .securityContext(sc -> sc
                        .securityContextRepository(userSecRepo)
                )

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
                        .failureHandler(formLoginFailureHandlerForUser)
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
                        .deleteCookies("remember-me")
                )

                .rememberMe(r -> r
                        .rememberMeServices(memberRememberMe())
                        .authenticationSuccessHandler(rememberMeSuccessHandler)
                )

                .addFilterAfter(new UserSessionSetupFilter(memberMapper),
                        RememberMeAuthenticationFilter.class);

        return http.build();
    }

    /* ======================
        SSE Security (별도 체인)
    ======================= */
    @Bean
    @Order(0)
    public SecurityFilterChain sseChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/sse/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(sc -> sc.disable())
                .requestCache(c -> c.disable())
                .anonymous(a -> a.disable());
        return http.build();
    }

    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
