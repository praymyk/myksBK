package com.myks.myksbk.domain.customer.dto;

import com.myks.myksbk.domain.customer.domain.CustomerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerSearchCondition {  // 검색조건 관리용
    private Long companyId;
    private String keyword;
    private CustomerStatus status;
}