package com.myks.myksbk.domain.category.controller;

import com.myks.myksbk.global.api.ApiResponse; // import 확인!
import com.myks.myksbk.domain.category.dto.CategoryDto;
import com.myks.myksbk.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 1. 조회 API
    @GetMapping("/page-data")
    public ApiResponse<CategoryDto.PageResponse> getCategoryPageData(
            @RequestParam Long companyId
    ) {
        return ApiResponse.ok(categoryService.getCategoryPageData(companyId));
    }

    // 2. 저장 API
    @PostMapping("/tree")
    public ApiResponse<Void> replaceCategoryTree(
            @RequestBody CategoryDto.TreeSaveRequest request
    ) {
        categoryService.replaceCategoryTree(request);

        return ApiResponse.success();
    }
}