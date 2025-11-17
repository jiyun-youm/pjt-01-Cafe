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
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> customAttributes = new HashMap<>(attributes);

        String email = null;
        String username = null;

        switch (provider) {
            case "naver" -> {
                Map<String, Object> res = (Map<String, Object>) attributes.get("response");
                if (res != null) {
                    email = (String) res.get("email");
                    username = (String) res.get("name");
                }
            }
            case "kakao" -> {
                Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
                if (account != null) {
                    email = (String) account.get("email");
                    Map<String, Object> profile = (Map<String, Object>) account.get("profile");
                    username = (profile != null) ? (String) profile.get("nickname") : "kakaoUser";
                }
            }
            default -> {
                email = (String) attributes.get("email");
                username = (String) attributes.get("name");
            }
        }

        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("no_email"), "이메일을 찾을 수 없습니다.");
        }

        customAttributes.put("email", email);

        MemberVO found = memberMapper.findByEmail(email);

        if (found != null) {
            if (!found.getProvider().equals(provider)) {
                // [핵심] 여기서 "provider=naver" 같은 힌트를 예외 메시지로 던집니다.
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("duplicate_login"),
                        "provider=" + found.getProvider()
                );
            }
            return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), customAttributes, "email");
        }

        MemberVO newMember = new MemberVO();
        newMember.setEmail(email);
        newMember.setId(email);
        newMember.setUsername(username);
        newMember.setProvider(provider);
        newMember.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        memberMapper.insertOAuthMember(newMember);

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), customAttributes, "email");
    }
}