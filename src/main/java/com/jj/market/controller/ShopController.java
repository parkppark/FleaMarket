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
    if (authentication != null && authentication.isAuthenticated() && 
        !"anonymousUser".equals(authentication.getPrincipal())) {
        
        Object principal = authentication.getPrincipal();
        String userIdentifier = null;
        
        if (principal instanceof DefaultOAuth2User) {
            DefaultOAuth2User oauth2User = (DefaultOAuth2User) principal;
            Map<String, Object> attributes = oauth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            userIdentifier = (String) kakaoAccount.get("email");
        } else {
            userIdentifier = authentication.getName();
        }
        
        boolean isOwner = false;
        if (product.getUser() != null) {
            isOwner = product.getUser().getEmail().equals(userIdentifier) || 
                     product.getUser().getUserID().equals(userIdentifier);
        }
        
        model.addAttribute("isAuthenticated", true);
        model.addAttribute("isOwner", isOwner);
    } else {
        model.addAttribute("isAuthenticated", false);
        model.addAttribute("isOwner", false);
    }
    
    return "shop-details";
}
}