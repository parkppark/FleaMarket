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
            // 모든 요청 매개변수 로깅
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
}