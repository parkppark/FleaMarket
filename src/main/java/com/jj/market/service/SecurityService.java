package com.jj.market.service;

import com.jj.market.entity.Product;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {
    
    public boolean isCurrentUserOwner(Product product) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        
        String currentUsername = auth.getName();
        return product != null && 
               product.getUser() != null && 
               currentUsername.equals(product.getUser().getUserID());
    }
}