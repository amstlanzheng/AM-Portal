package com.am.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtUtil {

    private JwtUtil() {}

    @NonNull
    public static String generateToken(String secret, String subject, Map<String, Object> claims, long expirationMs) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
        if (token == null) {
            // compact() 契约上不会返回 null，此处断言用于通过 IDE 空值分析
            throw new IllegalStateException("JWT compact() 不应返回 null");
        }
        return token;
    }

    public static Claims parseToken(String secret, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean isTokenExpired(String secret, String token) {
        try {
            Claims claims = parseToken(secret, token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public static String getSubject(String secret, String token) {
        return parseToken(secret, token).getSubject();
    }
}
