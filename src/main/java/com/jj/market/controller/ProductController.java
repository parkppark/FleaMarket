package com.jj.market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import com.jj.market.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import com.jj.market.entity.Product;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/productRegister")
    public String showProductRegisterForm(Model model) {
        model.addAttribute("currentPage", "productRegister");
        return "productRegister";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/productRegister")
    public String registerProduct(@RequestParam("p_imageUrl") MultipartFile[] files,
                               @RequestParam("p_name") String productName,
                               @RequestParam("p_category") String category,
                               @RequestParam("p_price") double price,
                               @RequestParam("p_status") String status,
                               @RequestParam("p_content") String content,
                               Authentication authentication) {
        try {
            log.info("요청 매개변수 확인:");
            log.info("p_name: {}", productName);
            log.info("p_category: {}", category);
            log.info("p_price: {}", price);
            log.info("p_status: {}", status);
            log.info("p_content: {}", content);
            log.info("파일 수: {}", files != null ? files.length : 0);
            
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    log.info("파일 {}: 이름={}, 크기={}", i, 
                          files[i].getOriginalFilename(), 
                          files[i].getSize());
                }
            }
            
            // 인증 확인
            if (authentication == null || !authentication.isAuthenticated()) {
                log.error("인증되지 않은 사용자 접근");
                return "redirect:/login";
            }
            
            String username = authentication.getName();
            if (username == null || username.isEmpty()) {
                log.error("유효하지 않은 사용자명");
                return "redirect:/login";
            }
            
            log.info("인증된 사용자명: {}", username);
            
            productService.registerProduct(productName, category, price,
                                        status, content, files, username);
            return "redirect:/shop?register=success";
        } catch (UsernameNotFoundException e) {
            log.error("사용자 정보 조회 실패: {}", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            log.error("상품 등록 실패: {}", e.getMessage(), e);
            return "redirect:/productRegister?error=true";
        }
    }

    @GetMapping("/product/modify/{id}")
    @PreAuthorize("isAuthenticated()")
    public String showModifyForm(@PathVariable Long id, Model model, Authentication authentication) {
        Product product = productService.getProductById(id);
        
        String currentUserId;
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof DefaultOAuth2User) {
            // 카카오 로그인의 경우
            DefaultOAuth2User oauth2User = (DefaultOAuth2User) principal;
            Map<String, Object> attributes = oauth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            currentUserId = kakaoAccount.get("email").toString();
            // 이메일로 비교
            if (!product.getUser().getEmail().equals(currentUserId)) {
                throw new AccessDeniedException("수정 권한이 없습니다.");
            }
        } else {
            // 일반 로그인의 경우
            currentUserId = authentication.getName();
            // userID로 비교
            if (!product.getUser().getUserID().equals(currentUserId)) {
                throw new AccessDeniedException("수정 권한이 없습니다.");
            }
        }
        
        model.addAttribute("product", product);
        return "productModify";
    }

    @PostMapping("/product/modify/{id}")
    @PreAuthorize("isAuthenticated()")
    public String modifyProduct(@PathVariable Long id,
                              @RequestParam("p_name") String productName,
                              @RequestParam("p_category") String category,
                              @RequestParam("p_price") double price,
                              @RequestParam("p_status") String status,
                              @RequestParam("p_content") String content,
                              @RequestParam(value = "p_imageUrl", required = false) MultipartFile[] files,
                              Authentication authentication) {
        
        Product product = productService.getProductById(id);
        

        String currentUserId;
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof DefaultOAuth2User) {

            DefaultOAuth2User oauth2User = (DefaultOAuth2User) principal;
            Map<String, Object> attributes = oauth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            currentUserId = kakaoAccount.get("email").toString();

            if (!product.getUser().getEmail().equals(currentUserId)) {
                throw new AccessDeniedException("수정 권한이 없습니다.");
            }
        } else {

            currentUserId = authentication.getName();

            if (!product.getUser().getUserID().equals(currentUserId)) {
                throw new AccessDeniedException("수정 권한이 없습니다.");
            }
        }
        
        productService.modifyProduct(id, productName, category, price, status, content, files, currentUserId);
        return "redirect:/shop/detail/" + id;
    }

    @PostMapping("/product/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProduct(@PathVariable Long id, Authentication authentication) {
        Product product = productService.getProductById(id);
        
        String currentUserId;
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof DefaultOAuth2User) {
            DefaultOAuth2User oauth2User = (DefaultOAuth2User) principal;
            Map<String, Object> attributes = oauth2User.getAttributes();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            currentUserId = kakaoAccount.get("email").toString();
            if (!product.getUser().getEmail().equals(currentUserId)) {
                throw new AccessDeniedException("삭제 권한이 없습니다.");
            }
        } else {
            currentUserId = authentication.getName();
            if (!product.getUser().getUserID().equals(currentUserId)) {
                throw new AccessDeniedException("삭제 권한이 없습니다.");
            }
        }
        
        productService.deleteProduct(id, currentUserId);
        return "redirect:/shop";
    }
}