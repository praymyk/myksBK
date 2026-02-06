package com.myks.myksbk.domain.template.controller;

import com.myks.myksbk.domain.template.dto.ResponseTemplateDto;
import com.myks.myksbk.domain.template.service.ResponseTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class ResponseTemplateController {

    private final ResponseTemplateService service;

    @GetMapping("/template")
    public ResponseEntity<?> getTemplates(
            @RequestParam Long companyId,
            @RequestParam String kind
    ) {
        List<ResponseTemplateDto.Response> list = service.getTemplates(companyId, kind);

        // 프론트 포맷: { ok: true, data: { rows: [...] } }
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "data", Map.of("rows", list)
        ));
    }

    @PostMapping("/response-templates")
    public ResponseEntity<?> saveTemplate(@RequestBody ResponseTemplateDto.SaveRequest request) {
        service.saveTemplate(request);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/response-templates/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        service.deleteTemplate(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}