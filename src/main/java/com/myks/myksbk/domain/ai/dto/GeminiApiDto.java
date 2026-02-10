package com.myks.myksbk.domain.ai.dto;

import java.util.List;

public class GeminiApiDto {

    // --- [요청: Spring -> Gemini] ---
    public record Request(List<Content> contents) {
        // 편의 생성자: 텍스트 하나만 보낼 때 사용
        public Request(String text) {
            this(List.of(new Content(List.of(new Part(text)))));
        }
    }

    // --- [응답: Gemini -> Spring] ---
    public record Response(List<Candidate> candidates) {
        // 응답 텍스트 추출 헬퍼 메서드
        public String getText() {
            if (candidates != null && !candidates.isEmpty()) {
                Content content = candidates.get(0).content();
                if (content != null && content.parts() != null && !content.parts().isEmpty()) {
                    return content.parts().get(0).text();
                }
            }
            return "";
        }
    }

    // --- [공통 내부 구조] ---
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
    public record Candidate(Content content) {}
}