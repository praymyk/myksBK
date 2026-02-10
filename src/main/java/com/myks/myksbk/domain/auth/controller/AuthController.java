package com.myks.myksbk.domain.auth.controller;

import com.myks.myksbk.domain.auth.dto.AuthDto;
import com.myks.myksbk.domain.auth.dto.LoginResult;
import com.myks.myksbk.domain.auth.service.AuthService;
import com.myks.myksbk.domain.user.domain.User;
import com.myks.myksbk.domain.user.domain.UserPreference;
import com.myks.myksbk.domain.user.repository.UserPreferenceRepository;
import com.myks.myksbk.domain.user.repository.UserRepository;
import com.myks.myksbk.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;

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
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request) {
        try {
            LoginResult result = authService.login(request);

            // 공통 메서드로 쿠키 생성 (중복 제거)
            ResponseCookie accessCookie = createCookie("accessToken", result.accessToken(), Duration.ofMinutes(30));
            ResponseCookie refreshCookie = createCookie("refreshToken", result.refreshToken(), Duration.ofDays(14));

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(result.response());

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Login failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Login Error", e);
            return ResponseEntity.status(500).body(new ErrorResponse("로그인 처리 중 오류가 발생했습니다."));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {

        ResponseCookie clearAccess = createCookie("accessToken", "", Duration.ZERO);
        ResponseCookie clearRefresh = createCookie("refreshToken", "", Duration.ZERO);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearAccess.toString())
                .header(HttpHeaders.SET_COOKIE, clearRefresh.toString())
                .build();
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<AuthDto.LoginResponse> me(@AuthenticationPrincipal Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean darkMode = userPreferenceRepository.findByUserId(userId)
                .map(UserPreference::isDarkMode)
                .orElse(false);

        AuthDto.LoginResponse body = AuthDto.LoginResponse.builder()
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

        return ResponseEntity.ok(body);
    }

    // 쿠키 생성 메서드
    private ResponseCookie createCookie(String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge);

        if (StringUtils.hasText(cookieDomain)) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    record ErrorResponse(String message) {}


    // 리플래시 토큰
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = getCookieValue(request, "refreshToken");

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String email = jwtTokenProvider.getEmail(refreshToken);

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, email);

        ResponseCookie accessCookie = createCookie("accessToken", newAccessToken, Duration.ofMinutes(30));

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        // Body에 토큰 반환
        return ResponseEntity.ok(new AuthDto.Response(newAccessToken));
    }

    private String getCookieValue(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        return Arrays.stream(req.getCookies())
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}