package com.myks.myksbk.domain.template.dto;

import com.myks.myksbk.domain.template.domain.ResponseTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ResponseTemplateDto {

    @Getter
    @NoArgsConstructor
    public static class SaveRequest {
        private Long companyId;
        private String kind;
        private String title;
        private String prompt;
        private String content;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;

        @JsonProperty("company_id")
        private Long companyId;

        private String kind;
        private String title;
        private String prompt;
        private String content;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;

        @JsonProperty("created_by")
        private Long createdBy;

        public static Response from(ResponseTemplate entity) {
            return Response.builder()
                    .id(entity.getId())
                    .companyId(entity.getCompanyId())
                    .kind(entity.getKind())
                    .title(entity.getTitle())
                    .prompt(entity.getPrompt())
                    .content(entity.getContent())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .createdBy(entity.getCreatedBy())
                    .build();
        }
    }
}