package com.myks.myksbk.domain.customer.repository;

import com.myks.myksbk.domain.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>,
                                            JpaSpecificationExecutor<Customer> {

    List<Customer> findByCompanyId(Long companyId);

}