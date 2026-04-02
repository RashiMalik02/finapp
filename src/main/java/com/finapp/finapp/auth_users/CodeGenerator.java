package com.finapp.finapp.auth_users;

import com.finapp.finapp.auth_users.repo.PasswordResetCodeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class CodeGenerator {
    private final PasswordResetCodeRepo repo;

    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 5;

    public String generateUniqueCode() {
        String code;
        do {
            code = generateRandomeCode();
        } while(repo.findByCode(code).isPresent());

        return code;
    }

    private String generateRandomeCode() {
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for(int i=0; i<CODE_LENGTH; i++) {
            int index = random.nextInt(ALPHA_NUMERIC.length());
            sb.append(ALPHA_NUMERIC.charAt(index));
        }
        return sb.toString();
    }
}
