package com.example.integratedcart.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // 외부 쇼핑몰 계정 정보 (AES-256 암호화 저장)
    @Column(name = "coupang_credentials")
    private String coupangCredentials;

    @Column(name = "kurly_credentials")
    private String kurlyCredentials;

    @Column(name = "bmart_credentials")
    private String bmartCredentials;

    @Builder
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void updateCredentials(String coupang, String kurly, String bmart) {
        if (coupang != null) this.coupangCredentials = coupang;
        if (kurly != null) this.kurlyCredentials = kurly;
        if (bmart != null) this.bmartCredentials = bmart;
    }
}
