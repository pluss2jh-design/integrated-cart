package com.example.integratedcart.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * AES-256 암호화/복호화 유틸리티.
 * 외부 쇼핑몰 계정 정보를 안전하게 암호화합니다.
 */
 @Component
public class AES256Util {

    private final String secretKey;

    // 환경변수 app.security.aes-secret 필수 설정 (32바이트)
    public AES256Util(@Value("${app.security.aes-secret}") String secretKey) {
        if (secretKey.length() < 32) {
            // 편의를 위해 길이를 맞춤 (실제 환경에서는 32바이트 키를 정확히 주입받아야 함)
            this.secretKey = String.format("%-32s", secretKey).substring(0, 32);
        } else {
            this.secretKey = secretKey.substring(0, 32);
        }
    }

    /**
     * 평문을 AES-256으로 암호화합니다.
     * @param text 암호화할 평문
     * @return Base64 인코딩된 암호문, null이면 null 반환
     * @throws Exception 암호화 실패 시
     */
    public String encrypt(String text) throws Exception {
        if (text == null) return null;
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES-256 암호문을 복호화합니다.
     * @param cipherText Base64 인코딩된 암호문
     * @return 복호화된 평문, null이면 null 반환
     * @throws Exception 복호화 실패 시
     */
    public String decrypt(String cipherText) throws Exception {
        if (cipherText == null) return null;
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        byte[] decrypted = cipher.doFinal(decodedBytes);
        return new String(decrypted);
    }
}
