package com.myks.myksbk.domain.template.repository;

import com.myks.myksbk.domain.template.domain.ResponseTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResponseTemplateRepository extends JpaRepository<ResponseTemplate, Long> {

    List<ResponseTemplate> findAllByCompanyIdAndKindAndDeletedAtIsNullOrderByCreatedAtDesc(Long companyId, String kind);
}