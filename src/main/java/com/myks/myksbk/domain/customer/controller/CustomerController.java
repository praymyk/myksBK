package com.myks.myksbk.domain.customer.controller;

import com.myks.myksbk.domain.customer.dto.CustomerDto;
import com.myks.myksbk.domain.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDto.Response> createCustomer(@RequestBody CustomerDto.CreateRequest request) {

        CustomerDto.Response newCustomer = customerService.createCustomer(request);

        return ResponseEntity.ok(newCustomer);
    }
}