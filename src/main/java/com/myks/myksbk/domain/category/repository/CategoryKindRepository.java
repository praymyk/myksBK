package com.myks.myksbk.domain.category.repository;

import com.myks.myksbk.domain.category.domain.CategoryKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryKindRepository extends JpaRepository<CategoryKind, Long> {

    List<CategoryKind> findAllByIsActiveTrueOrderByIdAsc();

    Optional<CategoryKind> findByCode(String code);
}