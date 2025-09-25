package com.gdg.Todak.member.util;

import com.gdg.Todak.common.exception.TodakException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import static com.gdg.Todak.common.exception.errors.MemberError.PASSWORD_ENCRYPTION_ERROR;

public class PasswordEncoder {

    public static String getSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String getEncodedPassword(String salt, String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] encodedPassword = md.digest((salt + rawPassword).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : encodedPassword) {
                sb.append(String.format("%02x", b));
            }
            return salt + sb;
        } catch (NoSuchAlgorithmException e) {
            throw new TodakException(PASSWORD_ENCRYPTION_ERROR);
        }
    }
}
