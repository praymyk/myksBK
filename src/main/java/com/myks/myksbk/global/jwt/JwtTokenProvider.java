package com.myks.myksbk.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 비밀키는 실무에선 application.yml에서 가져와야 하지만, 지금은 하드코딩으로 예시를 듭니다.
    // 32글자 이상이어야 안전합니다.
    private final String SECRET_KEY = "myks-secret-key-for-jwt-must-be-very-long-and-secure";
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    private final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24시간

    // 1. 토큰 생성
    public String createToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // 주제(Subject)에 userId 저장
                .claim("email", email)              // 추가 정보 저장
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. 토큰에서 userId 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    // 3. 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}