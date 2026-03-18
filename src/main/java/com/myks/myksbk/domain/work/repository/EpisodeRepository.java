package com.myks.myksbk.domain.work.repository;

import com.myks.myksbk.domain.work.domain.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    // 작품 ID로 조회하되, 회차 번호(episodeNo) 오름차순으로 정렬
    List<Episode> findByWorkIdOrderByEpisodeNoAsc(Long workId);
}