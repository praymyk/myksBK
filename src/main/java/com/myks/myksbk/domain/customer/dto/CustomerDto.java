package com.myks.myksbk.domain.customer.dto;

import com.myks.myksbk.domain.customer.domain.Customer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class CustomerDto {

    @Getter @Setter
    public static class CreateRequest {
        private Long companyId;
        private String name;
        private String email;
        private String status;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long comapnyId;
        private String name;
        private String email;
        private String status;
        private LocalDateTime createdAt;

        public static Response from(Customer entity) {
            return Response.builder()
                    .id(entity.getId())
                    .comapnyId(entity.getCompanyId())
                    .name(entity.getName())
                    .email(entity.getEmail())
                    .status(entity.getStatus().name())
                    .createdAt(entity.getCreatedAt())
                    .build();
        }
    }
}