package com.miniproject.cafe.Service;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String newProvider = registrationId; // 새로 로그인 시도한 provider (예: "naver")

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = null;
        String name = null;

        // --- Provider(제공자)별로 파싱 로직 분기 ---
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            email = (String) response.get("email");
            name = (String) response.get("name");
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
        } else if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }
        // --- 분기 끝 ---

        Optional<MemberVO> memberOptional = memberMapper.findByEmail(email);

        MemberVO member;
        if (memberOptional.isPresent()) {
            // [CASE 1, 2, 4] 이메일이 이미 존재하는 경우
            member = memberOptional.get();
            String existingProvider = member.getProvider(); // DB에서 가져온 provider 값

            // -------------------------------------------------------------
            // 1. [핵심] 일반 회원가입 계정 충돌 및 NullPointerException 처리
            //    existingProvider가 NULL이거나, "general"인 경우 충돌로 간주하고 차단
            // -------------------------------------------------------------
            if (existingProvider == null || "general".equals(existingProvider)) {

                String errorMessage = "이미 일반 회원가입(ID/PW)으로 등록된 이메일입니다. 일반 로그인으로 접속해주세요.";

                OAuth2Error error = new OAuth2Error("ALREADY_REGISTERED_LOCAL", errorMessage, null);
                throw new OAuth2AuthenticationException(error, errorMessage);

            } else if (existingProvider.equals(newProvider)) {
                // [CASE 1: 로그인 성공] 이메일도 같고 provider도 같음 (정상 로그인)

                if (!member.getUsername().equals(name)) {
                    member.setUsername(name);
                    memberMapper.updateUser(member);
                }

            } else {
                // [CASE 2: 소셜 계정 충돌] provider는 존재하지만 다름 (예: 카카오 계정으로 네이버 로그인 시도)
                String providerName = "";
                if ("kakao".equals(existingProvider)) providerName = "카카오";
                else if ("naver".equals(existingProvider)) providerName = "네이버";
                else if ("google".equals(existingProvider)) providerName = "구글";
                else providerName = existingProvider;

                String errorMessage = "이미 " + providerName + "로 가입된 이메일입니다. " + providerName + "로 로그인해주세요.";

                OAuth2Error error = new OAuth2Error("DUPLICATE_REGISTRATION", errorMessage, null);
                throw new OAuth2AuthenticationException(error, errorMessage);
            }

            // -------------------------------------------------------------

            // 기존 회원 로그인 성공 시 알림 메시지 세션 저장
            httpSession.setAttribute("loginSuccessType", "LOGIN");
            httpSession.setAttribute("loginMemberName", member.getUsername());


        } else {
            // [CASE 3: 회원가입] 신규 회원인 경우
            member = new MemberVO();
            member.setEmail(email);
            member.setUsername(name);
            member.setProvider(newProvider);
            member.setId(email);

            // 소셜 회원도 패스워드 암호화하여 DB에 저장 (더미 패스워드)
            String dummyPassword = UUID.randomUUID().toString();
            member.setPassword(passwordEncoder.encode(dummyPassword));

            memberMapper.insertOAuthMember(member);

            // 신규 가입 성공 시 알림 메시지 세션 저장
            httpSession.setAttribute("loginSuccessType", "SIGNUP");
            httpSession.setAttribute("loginMemberName", member.getUsername());
        }

        httpSession.setAttribute("loginMember", member);

        // Spring Security가 인증 처리를 할 수 있도록 DefaultOAuth2User 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                userNameAttributeName
        );
    }
}