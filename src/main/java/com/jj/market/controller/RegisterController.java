package com.jj.market.controller;

import com.jj.market.entity.User;
import com.jj.market.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j2
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String userid,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String name,
                           @RequestParam String addr,
                           @RequestParam String phone,
                           @RequestParam String sex) {

        User user = userService.register(userid, password, email, name, addr, phone, sex);

        if (user != null) {
            log.info("회원가입 성공: " + userid);
            return "redirect:/login?register=success";
        } else {
            log.info("회원가입 실패: " + userid);
            return "redirect:/register?error=true";
        }
    }
}