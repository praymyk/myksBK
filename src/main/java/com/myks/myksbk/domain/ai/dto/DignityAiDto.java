package com.myks.myksbk.domain.ai.dto;

import java.util.List;

public class DignityAiDto {
    // 프론트에서 받을 요청
    public record GenerateRequest(
            List<ItemDto> currentItems,
            List<ItemDto> futureItems
    ) {}

    public record ItemDto(String name, int price, int lifespan) {}

    // 프론트로 보낼 응답
    public record GenerateResponse(boolean ok, AiContent data) {}
    public record AiContent(String content) {}
}