package com.myks.myksbk.domain.user.dto;

public class UserPreferenceDto {

    public record UpdateRequest(
            Boolean darkMode,
            Integer defaultPageSize
    ) {}

    public record Response(
            Long userId,
            boolean darkMode,
            int defaultPageSize
    ) {
        public static Response from(Long userId, boolean darkMode, int defaultPageSize) {
            return new Response(userId, darkMode, defaultPageSize);
        }
    }
}