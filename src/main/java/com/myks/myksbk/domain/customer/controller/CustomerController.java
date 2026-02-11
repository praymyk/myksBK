package com.myks.myksbk.domain.customer.controller;

import com.myks.myksbk.domain.customer.dto.CustomerDto;
import com.myks.myksbk.domain.customer.dto.CustomerSearchCondition;
import com.myks.myksbk.domain.customer.service.CustomerService;
import com.myks.myksbk.global.api.ApiResponse;
import com.myks.myksbk.global.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ApiResponse<CustomerDto.Response> createCustomer(@RequestBody CustomerDto.CreateRequest request) {

        CustomerDto.Response newCustomer = customerService.createCustomer(request);

        return ApiResponse.ok(newCustomer);
    }

    @GetMapping
    public ApiResponse<PagedResponse<CustomerDto.Response>> getCustomers(
            CustomerSearchCondition condition,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        int pageNumber = Math.max(page, 1) - 1;

        Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));

        Page<CustomerDto.Response> result = customerService.searchCustomers(condition, pageable);

        return ApiResponse.ok(PagedResponse.from(result));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDto.Response> getCustomer(@PathVariable Long id) {
        CustomerDto.Response response = customerService.getCustomerById(id);
        return ApiResponse.ok(response);
    }
}