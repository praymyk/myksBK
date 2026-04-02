package com.myks.myksbk.domain.work.dto;

import com.myks.myksbk.domain.work.domain.WorkMode;
import com.myks.myksbk.domain.work.domain.WorkStatus;

import java.time.LocalDateTime;
import java.util.List;

public record WorkDetailResponse(
        Long id,
        Long companyId,
        Long authorUserId,
        String title,
        String description,
        WorkMode mode,
        Boolean aiImageEnabled,
        WorkStatus status,
        String thumbnailUrl,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
