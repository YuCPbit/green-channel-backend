package edu.greenchannel.student;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class StudentDataProtector {
    private final SecureRandom secureRandom = new SecureRandom();

    public String protect(String value) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return "sha256$" + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("敏感数据保护算法不可用", exception);
        }
    }
}
