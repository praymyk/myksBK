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
        // userId: 로그인한 유저의 ID가 자동으로 들어옵니다. (로그인 안했으면 null)

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 실패"));
        }

        UserPreferenceDto.Response response = preferenceService.getPreferences(userId);
        return ResponseEntity.ok(response);
    }

    // POST 요청
    @PostMapping
    public ResponseEntity<?> updatePreferences(@RequestBody UserPreferenceDto.UpdateRequest request) {
        Long currentUserId = 1L; // [임시] 로그인 구현 후 교체 필요!

        preferenceService.upsertPreferences(currentUserId, request);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}