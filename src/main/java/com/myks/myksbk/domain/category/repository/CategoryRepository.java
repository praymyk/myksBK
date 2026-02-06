package com.myks.myksbk.domain.category.repository;

import com.myks.myksbk.domain.category.domain.Category;
import com.myks.myksbk.domain.category.domain.CategoryKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByCompanyIdAndIsActiveTrueOrderByLevelAscSortOrderAsc(Long companyId);

    List<Category> findAllByCompanyIdAndKind(Long companyId, CategoryKind kind);

}