package com.myks.myksbk.domain.company.controller;

import com.myks.myksbk.domain.company.domain.CompanyStatus;
import com.myks.myksbk.domain.company.dto.CompanyDto;
import com.myks.myksbk.domain.company.service.CompanyService;
import com.myks.myksbk.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/active")
    public ApiResponse<List<CompanyDto.SelectResponse>> getActiveCompanies() {
        return ApiResponse.ok(companyService.getActiveCompanies(CompanyStatus.active));
    }

    /**
     * [관리자용] 상태 상관없이 전체 업체 조회
     */
    @GetMapping
    public ApiResponse<List<CompanyDto.SelectResponse>> getAllCompanies() {
        return ApiResponse.ok(companyService.getAllCompanies());
    }
}