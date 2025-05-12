package com.jj.market.service;

import com.jj.market.entity.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.jj.market.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import com.jj.market.dto.KakaoUserInfo;

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;

@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    if ("kakao".equals(userRequest.getClientRegistration().getRegistrationId())) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String nickname = (String) properties.get("nickname");
        String id = String.valueOf(attributes.get("id"));
        String email = (String) kakaoAccount.get("email");

        saveOrUpdate(id, nickname, email);

        // 기본 사용자 권한 설정
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new DefaultOAuth2User(
            authorities,  // 빈 리스트 대신 권한이 포함된 리스트 사용
            attributes,
            "id"
        );
    }

    return oAuth2User;
}

private void saveOrUpdate(String id, String nickname, String email) {
    // 먼저 이메일로 기존 회원 찾기
    Optional<User> existingUser = userRepository.findByEmail(email);
    
    if (existingUser.isPresent()) {
        // 기존 회원이 있다면 소셜 로그인 정보 업데이트
        User user = existingUser.get();
        user.setProvider("KAKAO");
        user.setProviderId(id);
        userRepository.save(user);
        return;
    }

    // 기존 회원이 없는 경우에만 새로운 회원 생성
    User newUser = User.builder()
        .username(nickname)
        .userID("kakao_" + id)
        .email(email)
        .provider("KAKAO")
        .providerId(id)
        .role("USER")
        .build();
    userRepository.save(newUser);
}

    public String getKakaoAccessToken(String code) {
        String reqURL = "https://kauth.kakao.com/oauth/token";
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "4ef903792704ba3070a9f6b491f7beb7"); // 카카오 앱 키로 변경 필요
        params.add("redirect_uri", "http://localhost:8080/login/oauth2/code/kakao");
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = 
            new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            reqURL,
            HttpMethod.POST,
            kakaoTokenRequest,
            String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public KakaoUserInfo getKakaoUserInfo(String accessToken) {
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = 
            new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            reqURL,
            HttpMethod.POST,
            kakaoUserInfoRequest,
            String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Long id = jsonNode.get("id").asLong();
            String nickname = jsonNode.get("properties").get("nickname").asText();
            String email = jsonNode.get("kakao_account").get("email").asText();

            return new KakaoUserInfo(id, email, nickname);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User processUserLogin(KakaoUserInfo userInfo) {
        Optional<User> existingUser = userRepository.findByEmail(String.valueOf(userInfo.getId()));

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User newUser = User.builder()
                .username(userInfo.getNickname())
                .userID("kakao_" + userInfo.getId())
                .email(userInfo.getEmail())
                .provider("KAKAO")
                .providerId(String.valueOf(userInfo.getId()))
                .role("USER")
                .build();

        return userRepository.save(newUser);
    }
}