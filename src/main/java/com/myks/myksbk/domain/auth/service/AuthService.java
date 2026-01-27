package com.myks.myksbk.domain.auth.service;

import com.myks.myksbk.domain.auth.dto.AuthDto;
import com.myks.myksbk.domain.auth.dto.LoginResult;
import com.myks.myksbk.domain.user.domain.User;
import com.myks.myksbk.domain.user.domain.UserPreference;
import com.myks.myksbk.domain.user.domain.UserStatus;
import com.myks.myksbk.domain.user.repository.UserPreferenceRepository;
import com.myks.myksbk.domain.user.repository.UserRepository;
import com.myks.myksbk.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(readOnly = true)
    public LoginResult login(AuthDto.LoginRequest request) {

        if (request.username() == null || request.password() == null) {
            throw new IllegalArgumentException("아이디와 비밀번호를 입력해주세요.");
        }

        User user = userRepository.findByAccountOrEmail(request.username())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (user.getStatus() != UserStatus.active) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        boolean darkMode = userPreferenceRepository.findByUserId(user.getId())
                .map(UserPreference::isDarkMode)
                .orElse(false);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        AuthDto.LoginResponse response = AuthDto.LoginResponse.builder()
                .message("OK")
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

        return new LoginResult(accessToken, refreshToken, response);
    }
}