package com.myks.myksbk.global.api;

public record ApiError(
        String code,
        String message
) {}