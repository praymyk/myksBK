package com.myks.myksbk.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessExpMs;
    private final long refreshExpMs;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-ms}") long accessExpMs,
            @Value("${jwt.refresh-exp-ms}") long refreshExpMs,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpMs = accessExpMs;
        this.refreshExpMs = refreshExpMs;
        this.issuer = issuer;
    }

    // tv 추가
    public String createAccessToken(Long userId, String email, int tokenVersion) {
        return createToken(userId, email, tokenVersion, "access", accessExpMs);
    }

    // tv 추가
    public String createRefreshToken(Long userId, String email, int tokenVersion) {
        return createToken(userId, email, tokenVersion, "refresh", refreshExpMs);
    }

    private String createToken(Long userId, String email, int tokenVersion, String typ, long expMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMs);

        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("typ", typ)
                .claim("tv", tokenVersion) // token version
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateTokenType(String token, String expectedTyp) {
        try {
            Claims c = parseClaims(token);
            String typ = c.get("typ", String.class);
            return expectedTyp.equals(typ);
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    // tv 꺼내기
    public int getTokenVersion(String token) {
        Integer tv = parseClaims(token).get("tv", Integer.class);
        return (tv == null) ? 0 : tv;
    }
}