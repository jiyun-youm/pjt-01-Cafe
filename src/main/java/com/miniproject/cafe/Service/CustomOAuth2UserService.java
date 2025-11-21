package com.miniproject.cafe.Service;

import com.miniproject.cafe.Mapper.MemberMapper;
import com.miniproject.cafe.VO.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

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

        // 1. 기본 정보 로드
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId(); // naver / kakao / google …

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> customAttributes = new HashMap<>(attributes);

        String originalEmail = null;   // 실제 제공자 이메일 (xxx@naver.com 등)
        String username      = null;   // 화면 표시 이름
        String providerId    = null;   // 각 SNS의 고유 ID

        // 2. Provider 별로 필요한 값 파싱
        switch (provider) {
            case "naver" -> {
                Map<String, Object> res = (Map<String, Object>) attributes.get("response");
                if (res != null) {
                    originalEmail = (String) res.get("email");
                    username      = (String) res.get("name");
                    providerId    = (String) res.get("id");
                }
            }
            case "kakao" -> {
                providerId = String.valueOf(attributes.get("id"));

                Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
                if (account != null) {
                    originalEmail = (String) account.get("email");
                    Map<String, Object> profile = (Map<String, Object>) account.get("profile");
                    if (profile != null) {
                        username = (String) profile.get("nickname");
                    }
                }
                if (username == null) {
                    username = "kakaoUser";
                }
            }
            default -> { // google 등
                originalEmail = (String) attributes.get("email");
                username      = (String) attributes.get("name");
                providerId    = String.valueOf(attributes.get("sub")); // 구글의 고유 ID(sub)
            }
        }

        if (providerId == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("no_provider_id"),
                    "소셜 계정 고유 ID(providerId)를 가져올 수 없습니다."
            );
        }

        // DB에 저장·로그인에 사용할 "가상 이메일" 생성
        // kakao_1234567890@oauth.com / naver_abcd@oauth.com
        String loginEmail = provider + "_" + providerId + "@oauth.com";

        customAttributes.put("email", loginEmail);
        if (originalEmail != null) {
            customAttributes.put("realEmail", originalEmail);
        }
        if (username != null) {
            customAttributes.put("name", username);
        }

        MemberVO found = memberMapper.findByEmail(loginEmail);

        if (found != null) {
            return new CustomUserDetails(found, customAttributes);
        }

        MemberVO newMember = new MemberVO();
        newMember.setEmail(loginEmail);
        newMember.setId(loginEmail);
        newMember.setUsername(username != null ? username : loginEmail);
        newMember.setProvider(provider);
        newMember.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // 랜덤 비밀번호

        memberMapper.insertOAuthMember(newMember);

        return new CustomUserDetails(newMember, customAttributes);
    }
}
