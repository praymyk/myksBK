package com.myks.myksbk.domain.ai.dto;

import java.util.List;

public class DignityAiDto {

    // 받을 요청
    public record GenerateRequest(
            List<ItemDto> currentItems,
            List<ItemDto> futureItems,
            Long monthlySalary
    ) {}

    public record ItemDto(String name, int price, int lifespan) {}

    // 보낼 응답 data(payload)
    public record AiContent(String content) {}
}