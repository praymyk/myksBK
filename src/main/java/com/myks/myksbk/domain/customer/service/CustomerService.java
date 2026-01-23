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

        // 1. 동적 쿼리(Specification)
        Specification<Customer> spec = (root, query, cb) -> {

            Specification<Customer> result = Specification.where((Specification<Customer>) null);

            // A. 키워드 검색
            if (StringUtils.hasText(condition.getKeyword())) {
                String likePattern = "%" + condition.getKeyword().trim() + "%";

                result = result.and((root2, query2, cb2) ->
                        cb2.or(
                                cb2.like(root2.get("name"), likePattern),
                                cb2.like(root2.get("email"), likePattern)
                        )
                );
            }

            // B. 상태 필터
            if (condition.getStatus() != null) {
                result = result.and((root2, query2, cb2) ->
                        cb2.equal(root2.get("status"), condition.getStatus())
                );
            }

            return result.toPredicate(root, query, cb);
        };

        Page<Customer> customerPage = customerRepository.findAll(spec, pageable);

        return customerPage.map(CustomerDto.Response::from);
    }
}