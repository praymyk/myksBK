package com.myks.myksbk.global.jwt;

import com.myks.myksbk.domain.user.repository.UserRepository;
import com.myks.myksbk.global.security.CustomUserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    private static final AntPathMatcher PATH = new AntPathMatcher();

    // 필터 스킵 경로들
    private static final Set<String> SKIP_PATTERNS = Set.of(
            "/uploads/**",
            "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 1) 프리플라이트는 인증 불필요
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String uri = request.getRequestURI();

        // permitAll auth endpoints만 스킵
        if (uri.equals("/api/auth/login")) return true;
        if (uri.equals("/api/auth/refresh")) return true;
        if (uri.equals("/api/auth/signup")) return true;
        if (uri.equals("/api/auth/check-account")) return true;
        if (uri.equals("/api/auth/check-email")) return true;

        for (String pattern : SKIP_PATTERNS) {
            if (PATH.match(pattern, uri)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 이미 인증이 잡혀있으면 중복 처리 X
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getJwtFromRequest(request);

        // 토큰이 없으면 그냥 통과 (SecurityConfig가 보호 리소스면 401 처리)
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 유효성 체크
            if (!tokenProvider.validateToken(token) || !tokenProvider.validateTokenType(token, "access")) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = tokenProvider.getUserId(token);
            if (userId == null) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            var userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                // 토큰은 유효하지만 DB에 사용자가 없음(탈퇴/정리) → 인증 세팅 안 함
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            var user = userOpt.get();

            Integer tokenVersion = tokenProvider.getTokenVersion(token);
            Integer dbVersion = user.getTokenVersion(); // User 엔티티에 tokenVersion 필드가 있어야 함

            // 토큰 버전이 없거나(DB/토큰) 불일치 ->  폐기된 토큰
            if (tokenVersion == null || dbVersion == null || !tokenVersion.equals(dbVersion)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // TODO(확장): roles/authorities 넣고 싶으면 여기서 구성
            List<GrantedAuthority> authorities = Collections.emptyList();

            CustomUserPrincipal principal = new CustomUserPrincipal(
                    user.getId(),
                    user.getCompanyId(),
                    user.getAccount(),
                    user.getName(),
                    user.getProfileName(),
                    authorities
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // 어떤 예외든 인증 세팅은 하지 않고 통과
            // (보호 자원이라면 SecurityConfig에서 401 처리)
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // 1) Authorization 헤더 우선
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
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