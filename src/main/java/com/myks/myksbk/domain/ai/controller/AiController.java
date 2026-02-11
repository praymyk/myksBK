package com.myks.myksbk.domain.ai.controller;

import com.myks.myksbk.domain.ai.dto.DignityAiDto;
import com.myks.myksbk.domain.ai.dto.TemplateAiDto;
import com.myks.myksbk.domain.ai.service.AiGenerationService;
import com.myks.myksbk.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiGenerationService aiService;

    @PostMapping("/generate/template")
    public ApiResponse<Map<String, String>> generateTemplate(
            @RequestBody TemplateAiDto.GenerateRequest request
    ) {
        String generatedContent = aiService.generateTemplate(request);
        return ApiResponse.ok(Map.of("content", generatedContent));
    }

    @PostMapping("/generate/dignity")
    public ApiResponse<DignityAiDto.AiContent> generateDignity(
            @RequestBody DignityAiDto.GenerateRequest request
    ) {
        String jsonContent = aiService.generateDignityAnalysis(request);
        return ApiResponse.ok(new DignityAiDto.AiContent(jsonContent));
    }
}