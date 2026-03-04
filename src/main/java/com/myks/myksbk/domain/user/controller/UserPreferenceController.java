package com.myks.myksbk.domain.user.controller;

import com.myks.myksbk.domain.user.dto.UserPreferenceDto;
import com.myks.myksbk.domain.user.service.UserPreferenceService;
import com.myks.myksbk.global.security.CustomUserPrincipal;
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

    @GetMapping
    public ResponseEntity<?> getPreferences(@AuthenticationPrincipal CustomUserPrincipal me) {
        if (me == null) return ResponseEntity.status(401).body(Map.of("message","인증 실패"));
        return ResponseEntity.ok(preferenceService.getPreferences(me.getId()));
    }

    @PostMapping
    public ResponseEntity<?> updatePreferences(@AuthenticationPrincipal CustomUserPrincipal me,
                                               @RequestBody UserPreferenceDto.UpdateRequest request) {
        if (me == null) return ResponseEntity.status(401).body(Map.of("message","로그인이 필요합니다."));
        preferenceService.upsertPreferences(me.getId(), request);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}