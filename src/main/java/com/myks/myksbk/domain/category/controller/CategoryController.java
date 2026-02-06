package com.myks.myksbk.domain.category.controller;

import com.myks.myksbk.domain.category.dto.CategoryDto;
import com.myks.myksbk.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 페이지 데이터 조회
    @GetMapping("/page-data")
    public ResponseEntity<CategoryDto.PageResponse> getCategoryPageData(
            @RequestParam Long companyId
    ) {
        CategoryDto.PageResponse response = categoryService.getCategoryPageData(companyId);
        return ResponseEntity.ok(response);
    }

    // 카테고리 트리 일괄 저장
    @PostMapping("/tree")
    public ResponseEntity<Void> replaceCategoryTree(
            @RequestBody CategoryDto.TreeSaveRequest request
    ) {
        categoryService.replaceCategoryTree(request);
        return ResponseEntity.ok().build();
    }
}