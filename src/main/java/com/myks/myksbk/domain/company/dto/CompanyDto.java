package com.myks.myksbk.domain.company.dto;

import com.myks.myksbk.domain.company.domain.Company;
import lombok.Builder;
import lombok.Getter;

public class CompanyDto {

    @Getter
    @Builder
    public static class SelectResponse {
        private Long id;
        private String name;

        public static SelectResponse from(Company company) {
            return SelectResponse.builder()
                    .id(company.getId())
                    .name(company.getName())
                    .build();
        }
    }
}