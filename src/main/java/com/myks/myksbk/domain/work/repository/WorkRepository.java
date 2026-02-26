package com.myks.myksbk.domain.work.repository;

import com.myks.myksbk.domain.work.domain.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkRepository extends JpaRepository<Work, Long> {

    List<Work> findByAuthorUserIdOrderByUpdatedAtDesc(Long authorUserId);

}