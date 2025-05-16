package com.jj.market.controller;

import com.jj.market.entity.Product;
import com.jj.market.entity.User;
import com.jj.market.service.ProductService;
import com.jj.market.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
@Slf4j
@Controller
@PreAuthorize("isAuthenticated()")
public class MyPageController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/myPage")
    public String myPage(Model model, Authentication authentication, HttpServletRequest request) {
        log.info("Authentication object: {}", authentication);
        log.info("Is authenticated: {}", authentication != null && authentication.isAuthenticated());
        log.info("Principal: {}", authentication.getPrincipal());

        if (authentication != null && authentication.isAuthenticated()) {
            String userID;
            if (authentication.getPrincipal() instanceof OAuth2User) {
                // OAuth2 로그인의 경우
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                Object idObj = oauth2User.getAttribute("id");
                userID = idObj != null ? idObj.toString() : "";
                log.info("카카오 로그인 provider_id: {}", userID);
                log.info("카카오 OAuth2User 속성들: {}", oauth2User.getAttributes());
            } else {
                // 일반 로그인의 경우
                userID = authentication.getName();
                log.info("일반 로그인 userID: {}", userID);
            }
        
            try {
                // 사용자 정보 가져오기
                User user = userService.getCurrentUser(userID);
                log.info("조회된 사용자 정보: {}", user);
            

                List<Product> userProducts = productService.getProductsByUserID(user.getUserID());  // 실제 userID 사용
                log.info("조회된 상품 개수: {}", userProducts.size());
            
                model.addAttribute("products", userProducts);
                model.addAttribute("username", user.getUsername());
            } catch (Exception e) {
                log.error("Error loading user data: ", e);
                model.addAttribute("products", new ArrayList<>());
                model.addAttribute("error", "사용자 데이터를 불러오는 중 오류가 발생했습니다.");
            }
        }
    
        return "myPage";
    }
}