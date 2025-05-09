package com.jj.market.controller;

import org.springframework.web.bind.annotation.GetMapping;
import com.jj.market.entity.User;
import com.jj.market.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import com.jj.market.service.CustomOAuth2UserService;

@Controller
@RequiredArgsConstructor
public class OAuth2Controller {

    private final CustomOAuth2UserService customOAuth2UserService;  // 이름 변경

    @GetMapping("/login/oauth2/code/kakao")
    public String kakaoCallback(String code) {
        // 카카오 액세스 토큰 받기
        String accessToken = customOAuth2UserService.getKakaoAccessToken(code);

        // 사용자 정보 가져오기
        KakaoUserInfo userInfo = customOAuth2UserService.getKakaoUserInfo(accessToken);

        // 회원가입 처리 또는 로그인 처리
        User user = customOAuth2UserService.processUserLogin(userInfo);

        return "redirect:/";
    }
}