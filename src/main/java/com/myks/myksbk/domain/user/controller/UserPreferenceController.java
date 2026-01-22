package com.myks.myksbk.domain.user.controller;

import com.myks.myksbk.domain.user.dto.UserPreferenceDto;
import com.myks.myksbk.domain.user.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/common/users/me/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    // GET 요청
    @GetMapping
    public ResponseEntity<?> getPreferences(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 실패"));
        }
        UserPreferenceDto.Response response = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(response);
    }

    // POST 요청
    @PostMapping
    public ResponseEntity<?> updatePreferences(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserPreferenceDto.UpdateRequest request
    ) {
        // 2. 로그인 안 된 경우 방어
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        preferenceService.upsertPreferences(userId, request);

        return ResponseEntity.ok(Map.of("ok", true));
    }
}