package com.myks.myksbk.domain.user.service;

import com.myks.myksbk.domain.user.domain.User;
import com.myks.myksbk.domain.user.domain.UserPreference;
import com.myks.myksbk.domain.user.dto.UserPreferenceDto;
import com.myks.myksbk.domain.user.repository.UserPreferenceRepository;
import com.myks.myksbk.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public UserPreferenceDto.Response getPreferences(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .map(pref -> UserPreferenceDto.Response.from(
                        userId,
                        pref.isDarkMode(),
                        pref.getDefaultPageSize()))
                .orElse(UserPreferenceDto.Response.from(userId, false, 20));
    }

    /** 사용자 설정 변경용 [ 초기 설정값 존재 하지 않아 있으면 수정 / 없으면 생성] */
    @Transactional
    public void upsertPreferences(Long userId, UserPreferenceDto.UpdateRequest request) {
        // 1. 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 설정값 조회 -> 있으면 수정, 없으면 생성
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreference.builder()
                        .user(user)
                        .darkMode(false)
                        .defaultPageSize(20)
                        .build());

        // 3. 값 업데이트 (null이 들어오면 기존 값 유지하거나 기본값 적용하는 로직)
        boolean newDarkMode = (request.darkMode() != null) ? request.darkMode() : preference.isDarkMode();
        int newPageSize = (request.defaultPageSize() != null) ? request.defaultPageSize() : 20;

        preference.update(newDarkMode, newPageSize);

        // 4. 저장 (신규 생성일 때만 save 호출이 필수지만, 명시적으로 호출해도 무방)
        preferenceRepository.save(preference);
    }
}