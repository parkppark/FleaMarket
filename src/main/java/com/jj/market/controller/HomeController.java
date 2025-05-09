package com.jj.market.controller;

import com.jj.market.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Model model, HttpSession session) {

        model.addAttribute("currentPage", "home");

        return "index";
    }

}
