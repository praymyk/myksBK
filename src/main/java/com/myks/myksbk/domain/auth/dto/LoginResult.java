package com.myks.myksbk.domain.auth.dto;

public record LoginResult(
        String accessToken,
        String refreshToken,
        AuthDto.LoginResponse response
) {}