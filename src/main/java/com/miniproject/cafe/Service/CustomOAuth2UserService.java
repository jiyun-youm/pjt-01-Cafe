package com.miniproject.cafe.Service;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // naver/kakao/google
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String username;

        switch (provider) {
            case "naver" -> {
                Map<String, Object> res = (Map<String, Object>) attributes.get("response");
                email = (String) res.get("email");
                username = (String) res.get("name");
            }
            case "kakao" -> {
                Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
                email = (String) account.get("email");
                Map<String, Object> profile = (Map<String, Object>) account.get("profile");
                username = profile != null ? (String) profile.get("nickname") : "kakaoUser";
            }
            default -> {
                email = (String) attributes.get("email");
                username = (String) attributes.get("name");
            }
        }

        if (email == null) {
            throw new OAuth2AuthenticationException("provider=" + provider + ":no_email");
        }

        MemberVO found = memberMapper.findByEmail(email);

        if (found != null) {

            // 🔥 provider 불일치 → 상세 메시지 생성
            if (!found.getProvider().equals(provider)) {
                throw new OAuth2AuthenticationException("provider=" + found.getProvider());
            }

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                    attributes,
                    "email");
        }

        // 신규 가입
        MemberVO newMember = new MemberVO();
        newMember.setEmail(email);
        newMember.setId(email);
        newMember.setUsername(username);
        newMember.setProvider(provider);
        newMember.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        memberMapper.insertOAuthMember(newMember);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "email");
    }
}
