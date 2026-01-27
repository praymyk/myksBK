package com.myks.myksbk.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 토큰 꺼내기
        String token = getJwtFromRequest(request);

        // 2. 토큰 유효 체크
        if (StringUtils.hasText(token)
                && tokenProvider.validateToken(token)
                && tokenProvider.validateTokenType(token, "access")) {

            Long userId = tokenProvider.getUserId(token);

            /*
             * TODO: Principal 객체화 + roles 확장 준비 ( 권한등 인증 정보 추가 )
             *
             * 현재:
             *  - principal = Long userId
             *  - authorities = Collections.emptyList()
             *  - 컨트롤러에서 @AuthenticationPrincipal Long userId 만 주입받아 사용 중
             *
             * 문제/한계:
             *  - userId 외 인증 정보를 뭉쳐서 보내기 어려움 -> 객체화 필요
             *
             * 전환 목표
             *  1) principal Long -> AuthPrincipal 같은 객체로 변경
             *
             *  2) 컨트롤러 변경:
             *     - @AuthenticationPrincipal Long -> @AuthenticationPrincipal AuthPrincipal
                   - userId는 principal.userId() 로 접근
             *
             *  4) SecurityConfig 확장
             *     - authorizeHttpRequests에 hasRole/hasAuthority 규칙 추가
             */

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.emptyList()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // 헤더에서 Bearer 토큰 추출
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1) Authorization 헤더 우선
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        // 2) accessToken 쿠키 fallback
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if ("accessToken".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}