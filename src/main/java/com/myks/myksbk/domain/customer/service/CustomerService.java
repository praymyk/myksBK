package com.myks.myksbk.domain.customer.service;

import com.myks.myksbk.domain.customer.domain.Customer;
import com.myks.myksbk.domain.customer.domain.CustomerStatus;
import com.myks.myksbk.domain.customer.dto.CustomerDto;
import com.myks.myksbk.domain.customer.dto.CustomerSearchCondition;
import com.myks.myksbk.domain.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    public Page<CustomerDto.Response> searchCustomers(CustomerSearchCondition condition, Pageable pageable) {

        Specification<Customer> spec = (root, query, cb) -> cb.conjunction();

        // companyId는 필수)
        if (condition.getCompanyId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("companyId"), condition.getCompanyId())
            );
        }

        // A. 키워드 검색
        if (StringUtils.hasText(condition.getKeyword())) {
            String likePattern = "%" + condition.getKeyword().trim() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(root.get("name"), likePattern),
                            cb.like(root.get("email"), likePattern)
                    )
            );
        }

        // B. 상태 필터
        if (condition.getStatus() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), condition.getStatus())
            );
        }

        return customerRepository.findAll(spec, pageable)
                .map(CustomerDto.Response::from);
    }

    public CustomerDto.Response getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 고객입니다. id=" + id));

        return toResponse(customer);
    }

    private CustomerDto.Response toResponse(Customer customer) {
        return CustomerDto.Response.builder()
                .id(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .status(customer.getStatus().name())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}