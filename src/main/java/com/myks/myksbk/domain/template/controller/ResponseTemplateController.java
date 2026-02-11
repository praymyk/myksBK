package com.myks.myksbk.domain.template.controller;

import com.myks.myksbk.domain.template.dto.ResponseTemplateDto;
import com.myks.myksbk.domain.template.service.ResponseTemplateService;
import com.myks.myksbk.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class ResponseTemplateController {

    private final ResponseTemplateService service;

    @GetMapping("/template")
    public ApiResponse<?> getTemplates(
            @RequestParam Long companyId,
            @RequestParam String kind
    ) {
        List<ResponseTemplateDto.Response> list = service.getTemplates(companyId, kind);

        return ApiResponse.ok(Map.of("rows", list));
    }

    @PostMapping("/response-templates")
    public ApiResponse<?> saveTemplate(@RequestBody ResponseTemplateDto.SaveRequest request) {
        service.saveTemplate(request);
        return ApiResponse.success();
    }

    @DeleteMapping("/response-templates/{id}")
    public ApiResponse<?> deleteTemplate(@PathVariable Long id) {
        service.deleteTemplate(id);
        return ApiResponse.success();
    }
}