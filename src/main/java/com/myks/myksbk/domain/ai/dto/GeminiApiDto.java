package com.myks.myksbk.domain.ai.dto;

import java.util.List;

public class GeminiApiDto {

    public record Request(List<Content> contents) {
        public Request(String text) {
            this(List.of(new Content(List.of(new Part(text)))));
        }
    }

    public record Response(List<Candidate> candidates) {
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

    public record Content(List<Part> parts) {}
    public record Part(String text) {}
    public record Candidate(Content content) {}
}