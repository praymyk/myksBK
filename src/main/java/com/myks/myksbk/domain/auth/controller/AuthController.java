package com.myks.myksbk.domain.auth.controller;

import com.myks.myksbk.domain.auth.dto.AuthDto;
import com.myks.myksbk.domain.auth.dto.LoginResult;
import com.myks.myksbk.domain.auth.service.AuthService;
import com.myks.myksbk.domain.user.domain.User;
import com.myks.myksbk.domain.user.domain.UserPreference;
import com.myks.myksbk.domain.user.repository.UserPreferenceRepository;
import com.myks.myksbk.domain.user.repository.UserRepository;
import com.myks.myksbk.global.api.ApiResponse;
import com.myks.myksbk.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    private final JwtTokenProvider jwtTokenProvider;

    // --- [설정값 주입] ---
    @Value("${jwt.cookie.domain:}")
    private String cookieDomain;

    @Value("${jwt.cookie.secure}")
    private boolean cookieSecure;

    @Value("${jwt.cookie.samesite}")
    private String cookieSameSite;

    // 로그인
    @PostMapping("/login")
    public ApiResponse<AuthDto.TokenResponse> login(
            @RequestBody AuthDto.LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult result = authService.login(request);

        ResponseCookie refreshCookie = createCookie("refreshToken", result.refreshToken(), Duration.ofDays(14));
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ApiResponse.ok(new AuthDto.TokenResponse(result.accessToken(), result.response()));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        ResponseCookie clearRefresh = createCookie("refreshToken", "", Duration.ZERO);
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());

        return ApiResponse.success();
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<AuthDto.MeResponse> me(@AuthenticationPrincipal Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean darkMode = userPreferenceRepository.findByUserId(userId)
                .map(UserPreference::isDarkMode)
                .orElse(false);

        AuthDto.MeResponse body = AuthDto.MeResponse.builder()
                .user(AuthDto.UserInfo.builder()
                        .id(user.getId())
                        .companyId(user.getCompanyId())
                        .name(user.getName())
                        .profileName(user.getProfileName())
                        .email(user.getEmail())
                        .extension(user.getExtension())
                        .build())
                .preferences(AuthDto.UserPreferences.builder()
                        .darkMode(darkMode)
                        .build())
                .build();

        return ApiResponse.ok(body);
    }

    // 쿠키 생성 메서드
    private ResponseCookie createCookie(String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)       // 환경변수 적용
                .sameSite(cookieSameSite)   // 환경변수 적용
                .path("/")
                .maxAge(maxAge);

        // 로컬 환경 등에서 domain 값이 없을 경우, 아예 설정을 안 해야(null) 브라우저가 localhost로 인식함
        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    // 리프레시 토큰으로 액세스 토큰 재발급
    @PostMapping("/refresh")
    public ApiResponse<AuthDto.TokenRefreshResponse> refresh(
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getCookieValue(request, "refreshToken");

        if (!StringUtils.hasText(refreshToken)) {
            throw new com.myks.myksbk.global.exception.UnauthorizedException("리프레시 토큰이 존재하지 않습니다. 다시 로그인해주세요.");
        }

        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new com.myks.myksbk.global.exception.UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
            }

            Long userId = jwtTokenProvider.getUserId(refreshToken);
            String email = jwtTokenProvider.getEmail(refreshToken);

            String newAccessToken = jwtTokenProvider.createAccessToken(userId, email);

            return ApiResponse.ok(new AuthDto.TokenRefreshResponse(newAccessToken));

        } catch (Exception e) {
            log.warn("Refresh Token 검증 실패: {}", e.getMessage());
            throw new com.myks.myksbk.global.exception.UnauthorizedException("리프레시 토큰이 만료되었거나 손상되었습니다.");
        }
    }

    private String getCookieValue(jakarta.servlet.http.HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        return java.util.Arrays.stream(req.getCookies())
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);
    }

}

