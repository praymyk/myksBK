package com.myks.myksbk.domain.customer.service;

import com.myks.myksbk.domain.customer.domain.Customer;
import com.myks.myksbk.domain.customer.domain.CustomerStatus;
import com.myks.myksbk.domain.customer.dto.CustomerDto;
import com.myks.myksbk.domain.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDto.Response createCustomer(CustomerDto.CreateRequest input) {

        Customer customer = new Customer();
        customer.setCompanyId(input.getCompanyId());
        customer.setName(input.getName());
        customer.setEmail(input.getEmail());
        customer.setStatus(CustomerStatus.valueOf(input.getStatus())); // String -> Enum 변환

        // insert > insert 완료 객체 반납
        Customer savedCustomer = customerRepository.save(customer);
        return CustomerDto.Response.from(savedCustomer);
    }
}