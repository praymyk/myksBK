package com.myks.myksbk.domain.user.controller;

import com.myks.myksbk.domain.user.dto.UserMeDto;
import com.myks.myksbk.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/common/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getMe(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 실패"));
        }

        UserMeDto.Response dto = userService.getMe(userId);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping
    public ResponseEntity<?> updateMe(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserMeDto.UpdateRequest request // DTO로 본문 받기
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 실패"));
        }

        userService.updateProfile(userId, request);
        UserMeDto.Response updatedProfile = userService.getMe(userId);

        return ResponseEntity.ok(updatedProfile);
    }

}
