package com.myks.myksbk.domain.ai.dto;

public class TemplateAiDto {
    public record GenerateRequest(
            String kind,
            String prompt
    ) {}
}
