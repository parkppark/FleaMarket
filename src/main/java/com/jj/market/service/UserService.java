package com.jj.market.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.jj.market.entity.User;
import com.jj.market.repository.UserRepository;
import java.util.Arrays;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String userID) throws UsernameNotFoundException {
        log.info("Attempting to load user: {}", userID);
        User user = userRepository.findByUserID(userID);
        if (user == null) {
            log.error("User not found: {}", userID);
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userID);
        }
        log.info("Found user: {}", user);
        
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            user.getUserID(),
            user.getPassword(),
            Arrays.asList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
        log.info("Created UserDetails: {}, Authorities: {}", 
             userDetails.getUsername(), userDetails.getAuthorities());
        
        return userDetails;
    }

    public User findByUserID(String userID) {
        User user = userRepository.findByUserID(userID);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userID);
        }
        return user;
    }

    public User login(String userID, String password) {
        User user = userRepository.findByUserID(userID);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public User register(String userID, String password, String email, String name,
                    String addr, String phone, String sex) {
        if (userRepository.findByUserID(userID) != null) {
            return null;
        }

        User user = User.builder()
            .userID(userID)
            .password(passwordEncoder.encode(password))
            .email(email)
            .username(name)
            .addr(addr)
            .phone(phone)
            .sex(sex)
            .role("USER")
            .build();

        return userRepository.save(user);
    }

    public User getCurrentUser(String userID) {
        log.info("사용자 조회 시도: {}", userID);
        
        User user = userRepository.findByUserID(userID);
        if (user == null) {
            user = userRepository.findByProviderId(userID);
        }
        
        if (user == null) {
            log.error("사용자를 찾을 수 없음: {}", userID);
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userID);
        }
        
        log.info("사용자 찾음: {}", user.getUserID());
        return user;
    }
}