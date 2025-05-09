package com.jj.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String userID;
    private String password;
    private String email;
    private String role;
    private String addr;
    private String phone;
    private String sex;
    
    // OAuth2 관련 필드 추가
    private String provider;    // 인증 제공자 (ex: "KAKAO")
    private String providerId;  // 인증 제공자에서의 ID
}