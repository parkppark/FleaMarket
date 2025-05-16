package com.jj.market.controller;

import com.jj.market.entity.Product;
import com.jj.market.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class ShopController {
    
    private final ProductService productService;
    
    public ShopController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping("/shop")
    public String shopPage(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("currentPage", "shop");
        return "shop";
    }

@GetMapping("/shop/detail/{id}")
public String showProductDetail(@PathVariable Long id, Model model) {
    Product product = productService.getProductById(id);
    model.addAttribute("product", product);
    model.addAttribute("currentPage", "shop");
    
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isOwner = false;
    
    if (authentication != null && authentication.isAuthenticated() && 
        !"anonymousUser".equals(authentication.getPrincipal())) {
        
        String currentUserId;
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof DefaultOAuth2User) {
            // 카카오 로그인의 경우
            DefaultOAuth2User oauth2User = (DefaultOAuth2User) principal;
            Map<String, Object> attributes = oauth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            currentUserId = kakaoAccount.get("email").toString();
            // 이메일로 비교
            isOwner = product.getUser().getEmail().equals(currentUserId);
        } else {
            // 일반 로그인의 경우
            currentUserId = authentication.getName();
            // userID로 비교
            isOwner = product.getUser().getUserID().equals(currentUserId);
        }
    }
    
    model.addAttribute("isAuthenticated", authentication != null && authentication.isAuthenticated());
    model.addAttribute("isOwner", isOwner);
    
    return "shop-details";
}
}