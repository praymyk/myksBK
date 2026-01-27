package com.myks.myksbk.domain.auth.controller;

import com.myks.myksbk.domain.auth.dto.AuthDto;
import com.myks.myksbk.domain.auth.dto.LoginResult;
import com.myks.myksbk.domain.auth.service.AuthService;
import com.myks.myksbk.domain.user.domain.User;
import com.myks.myksbk.domain.user.domain.UserPreference;
import com.myks.myksbk.domain.user.repository.UserPreferenceRepository;
import com.myks.myksbk.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest request) {
        try {
            LoginResult result = authService.login(request);

            ResponseCookie accessCookie = ResponseCookie.from("accessToken", result.accessToken())
                    .httpOnly(true)
                    .secure(false)    // TODO : 운영 https면 true로
                    .sameSite("Lax")  // 운영 cross-site면 None + secure(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(30))
                    .build();

            ResponseCookie refreshCookie = ResponseCookie
                    .from("refreshToken", result.refreshToken())
                    .httpOnly(true)
                    .secure(false)        // TODO : 운영 https면 true로
                    .sameSite("Lax")      // 운영 cross-site면 None + secure(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(30))
                    .build();

            // body에는 accessToken을 내려줌 (프론트는 메모리에만 저장)
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(result.response());

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ErrorResponse("로그인 처리 중 오류가 발생했습니다."));
        }
    }

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

    // 에러 응답용 record
    record ErrorResponse(String message) {}
}