package com.myks.myksbk.global.dto;

import lombok.Data;

public class TemplateAiDto {

    @Data
    public static class GenerateRequest {
        private String kind;
        private String prompt;
    }

    @Data
    public static class GenerateResponse {
        private String content;
    }
}