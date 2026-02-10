package com.myks.myksbk.domain.auth.dto;

import lombok.Builder;
import lombok.Data;

public class AuthDto {

    // 1. 로그인 요청
    public record LoginRequest(String username, String password) {}

    // 2. 로그인 응답
    @Data
    @Builder
    public static class LoginResponse {
        private String message;
        private UserInfo user;
        private UserPreferences preferences;
    }

    // 3. 내정보 확인 (프로필용)
    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private Long companyId;
        private String name;
        private String profileName;
        private String email;
        private String extension;
    }

    @Data
    @Builder
    public static class UserPreferences {
        private boolean darkMode;
    }

    // 4. 내정보 확인 (공용)
    @Data
    @Builder
    public static class MeResponse {
        private UserInfo user;
        private UserPreferences preferences;
    }

    public record Response(String message) {}
}