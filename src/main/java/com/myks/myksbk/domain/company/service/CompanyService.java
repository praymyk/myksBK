package com.myks.myksbk.domain.company.service;

import com.myks.myksbk.domain.company.domain.CompanyStatus;
import com.myks.myksbk.domain.company.dto.CompanyDto;
import com.myks.myksbk.domain.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<CompanyDto.SelectResponse> getActiveCompanies(CompanyStatus status) {

        return companyRepository.findAllByStatusOrderByNameAsc(status).stream()

                .map(CompanyDto.SelectResponse::from)
                .collect(Collectors.toList());
    }

    public List<CompanyDto.SelectResponse> getAllCompanies() {

        return companyRepository.findAllByOrderByNameAsc().stream()

                .map(CompanyDto.SelectResponse::from)
                .collect(Collectors.toList());
    }
}