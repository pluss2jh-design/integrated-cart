package com.example.integratedcart.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class AES256Util {

    private final String secretKey;

    public AES256Util(@Value("${app.security.aes-secret:ThisIsASecretKeyForAES256Encrypt}") String secretKey) {
        if (secretKey.length() < 32) {
            // 편의를 위해 길이를 맞춤 (실제 환경에서는 32바이트 키를 정확히 주입받아야 함)
            this.secretKey = String.format("%-32s", secretKey).substring(0, 32);
        } else {
            this.secretKey = secretKey.substring(0, 32);
        }
    }

    public String encrypt(String text) throws Exception {
        if (text == null) return null;
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(text.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

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
