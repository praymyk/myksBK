package com.myks.myksbk.domain.ai.controller;

import com.myks.myksbk.domain.ai.service.AiGenerationService;
import com.myks.myksbk.global.dto.TemplateAiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiGenerationService aiService;

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody TemplateAiDto.GenerateRequest request) {
        String generatedContent = aiService.generateTemplate(request);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "data", Map.of("content", generatedContent)
        ));
    }
}