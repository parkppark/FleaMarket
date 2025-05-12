package com.jj.market.controller;

import com.jj.market.entity.Product;
import com.jj.market.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

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
        return "shop-details";

    }


}