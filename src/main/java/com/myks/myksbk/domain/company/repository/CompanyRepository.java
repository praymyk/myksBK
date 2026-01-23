package com.myks.myksbk.domain.company.repository;

import com.myks.myksbk.domain.company.domain.Company;
import com.myks.myksbk.domain.company.domain.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findAllByOrderByNameAsc();

    List<Company> findAllByStatusOrderByNameAsc(CompanyStatus status);
}