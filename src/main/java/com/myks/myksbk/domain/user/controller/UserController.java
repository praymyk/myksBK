package com.myks.myksbk.domain.user.controller;

import com.myks.myksbk.domain.user.dto.UserMeDto;
import com.myks.myksbk.domain.user.service.UserService;
import com.myks.myksbk.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/common/users/me") // 예시
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getMe(@AuthenticationPrincipal CustomUserPrincipal me) {
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 실패"));
        }

        UserMeDto.Response dto = userService.getMe(me.getId());
        return ResponseEntity.ok(dto);
    }

    @PatchMapping
    public ResponseEntity<?> updateMe(
            @AuthenticationPrincipal CustomUserPrincipal me,
            @RequestBody UserMeDto.UpdateRequest request
    ) {
        if (me == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증 실패"));
        }

        userService.updateProfile(me.getId(), request);
        UserMeDto.Response updatedProfile = userService.getMe(me.getId());

        return ResponseEntity.ok(updatedProfile);
    }
}
