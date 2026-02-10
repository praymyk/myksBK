package com.myks.myksbk.domain.ai.dto;

import java.util.List;

public class GeminiDto {
    // Gemini API는 "contents" 배열 안에 "parts" 배열이 있는 구조를 요구
    public record GeminiRequest(List<Content> contents) {
        public GeminiRequest(String text) {
            this(List.of(new Content(List.of(new Part(text)))));
        }
    }

    // --- [공통 DTO] ---
    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    // --- [응답 DTO] ---
    public record GeminiResponse(List<Candidate> candidates) {}

    public record Candidate(Content content) {
        // 응답 텍스트를 바로 꺼내는 유틸 메서드
        public String getText() {
            if (content != null && content.parts() != null && !content.parts().isEmpty()) {
                return content.parts().get(0).text();
            }
            return "";
        }
    }
}
