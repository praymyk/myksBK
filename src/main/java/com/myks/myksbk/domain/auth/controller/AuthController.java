package com.myks.myksbk.domain.auth.controller;

import com.myks.myksbk.domain.auth.dto.AuthDto;
import com.myks.myksbk.domain.auth.dto.LoginResult;
import com.myks.myksbk.domain.auth.service.AuthService;
import com.myks.myksbk.domain.user.domain.User;
import com.myks.myksbk.domain.user.domain.UserPreference;
import com.myks.myksbk.domain.user.dto.SignupRequestDto;
import com.myks.myksbk.domain.user.repository.UserPreferenceRepository;
import com.myks.myksbk.domain.user.repository.UserRepository;
import com.myks.myksbk.domain.user.service.UserService;
import com.myks.myksbk.global.api.ApiResponse;
import com.myks.myksbk.global.exception.UnauthorizedException;
import com.myks.myksbk.global.jwt.JwtTokenProvider;
import com.myks.myksbk.global.security.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
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

    /**
     * 로그인 성공 시 access token 응답과 refresh token 쿠키를 함께 발급한다.
     */
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

    /**
     * 로그아웃 시 refresh cookie를 제거하고, 식별 가능한 사용자의 tokenVersion을 증가시켜 기존 토큰을 무효화한다.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal CustomUserPrincipal me,
            jakarta.servlet.http.HttpServletRequest request,
            HttpServletResponse response
    ) {
        ResponseCookie clearRefresh = createCookie("refreshToken", "", Duration.ZERO);
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());

        User logoutUser = resolveLogoutUser(me, request);
        if (logoutUser == null) {
            return ApiResponse.success();
        }

        logoutUser.bumpTokenVersion();
        userRepository.save(logoutUser);

        return ApiResponse.success();
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ApiResponse<AuthDto.MeResponse> me(@AuthenticationPrincipal CustomUserPrincipal me) {
        Long userId = me.getId();

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

    /**
     * refresh token 저장/삭제에 사용할 HttpOnly 쿠키를 생성한다.
     */
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

    /**
     * refresh token 쿠키를 검증해 새 access token을 재발급한다.
     */
    @PostMapping("/refresh")
    public ApiResponse<AuthDto.TokenRefreshResponse> refresh(
            jakarta.servlet.http.HttpServletRequest request
    ) {
        String refreshToken = getCookieValue(request, "refreshToken");

        if (!StringUtils.hasText(refreshToken)) {
            throw new com.myks.myksbk.global.exception.UnauthorizedException("리프레시 토큰이 존재하지 않습니다. 다시 로그인해주세요.");
        }

        try {
            if (!jwtTokenProvider.validateToken(refreshToken)
                    || !jwtTokenProvider.validateTokenType(refreshToken, "refresh")) {
                throw new com.myks.myksbk.global.exception.UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
            }

            Long userId = jwtTokenProvider.getUserId(refreshToken);
            String email = jwtTokenProvider.getEmail(refreshToken);
            int tokenTv = jwtTokenProvider.getTokenVersion(refreshToken);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new com.myks.myksbk.global.exception.UnauthorizedException("존재하지 않는 사용자입니다."));

            // 토큰 버전 불일치 = 폐기된 토큰
            Integer dbTv = user.getTokenVersion();
            if (dbTv == null || dbTv.intValue() != tokenTv) {
                throw new UnauthorizedException("이미 만료된 토큰입니다. 다시 로그인해주세요.");
            }

            // tv 포함 새 accessToken 발급
            String newAccessToken = jwtTokenProvider.createAccessToken(userId, email, user.getTokenVersion());

            return ApiResponse.ok(new AuthDto.TokenRefreshResponse(newAccessToken));

        } catch (UnauthorizedException e) {
            // 의도적으로 막은 케이스(버전 불일치 등)
            log.info("Refresh rejected: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("Refresh Token 검증 실패", e);
            throw new UnauthorizedException("리프레시 토큰이 만료되었거나 손상되었습니다.");
        }
    }

    /**
     * 로그아웃 대상 사용자를 access 인증 정보 또는 refresh token 쿠키에서 찾는다.
     */
    private User resolveLogoutUser(
            CustomUserPrincipal me,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        if (me != null && me.getId() != null) {
            return userRepository.findById(me.getId()).orElse(null);
        }

        String refreshToken = getCookieValue(request, "refreshToken");
        if (!StringUtils.hasText(refreshToken)) {
            return null;
        }

        try {
            if (!jwtTokenProvider.validateToken(refreshToken)
                    || !jwtTokenProvider.validateTokenType(refreshToken, "refresh")) {
                return null;
            }

            Long userId = jwtTokenProvider.getUserId(refreshToken);
            int tokenTv = jwtTokenProvider.getTokenVersion(refreshToken);

            return userRepository.findById(userId)
                    .filter(user -> user.getTokenVersion() == tokenTv)
                    .orElse(null);
        } catch (Exception e) {
            log.info("Logout fallback via refresh token skipped: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 요청 쿠키에서 지정한 이름의 값을 조회한다.
     */
    private String getCookieValue(jakarta.servlet.http.HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        return java.util.Arrays.stream(req.getCookies())
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .map(jakarta.servlet.http.Cookie::getValue)
                .orElse(null);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequestDto request) {
        userService.createUser(request);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/check-account")
    public ResponseEntity<ApiResponse<Void>> checkAccount(@RequestParam("account") String account) {
        if (userService.isAccountDuplicate(account)) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("DUPLICATE_ACCOUNT", "이미 사용 중인 아이디입니다."));
        }

        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Void>> checkEmail(@RequestParam("email") String email) {
        if (userService.isEmailDuplicate(email)) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("DUPLICATE_EMAIL", "이미 가입된 이메일입니다."));
        }

        return ResponseEntity.ok(ApiResponse.success());
    }

}
