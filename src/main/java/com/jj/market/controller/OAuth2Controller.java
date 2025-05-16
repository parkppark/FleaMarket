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

    private final CustomOAuth2UserService customOAuth2UserService;

    @GetMapping("/login/oauth2/code/kakao")
    public String kakaoCallback(String code) {

        String accessToken = customOAuth2UserService.getKakaoAccessToken(code);


        KakaoUserInfo userInfo = customOAuth2UserService.getKakaoUserInfo(accessToken);


        User user = customOAuth2UserService.processUserLogin(userInfo);

        return "redirect:/";
    }
}