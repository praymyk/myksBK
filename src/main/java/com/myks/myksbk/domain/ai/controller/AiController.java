package com.myks.myksbk.domain.ai.controller;

import com.myks.myksbk.domain.ai.dto.DignityAiDto;
import com.myks.myksbk.domain.ai.dto.TemplateAiDto;
import com.myks.myksbk.domain.ai.service.AiGenerationService;
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
    public ResponseEntity<?> generateTemplate(@RequestBody TemplateAiDto.GenerateRequest request) {
        String generatedContent = aiService.generateTemplate(request);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "data", Map.of("content", generatedContent)
        ));
    }

    @PostMapping("/generate/dignity")
    public ResponseEntity<DignityAiDto.GenerateResponse> generateDignity(@RequestBody DignityAiDto.GenerateRequest request) {

        // 서비스에서 이미 JSON String 형태로 받아옴
        String jsonContent = aiService.generateDignityAnalysis(request);

        return ResponseEntity.ok(
                new DignityAiDto.GenerateResponse(true, new DignityAiDto.AiContent(jsonContent))
        );
    }

}