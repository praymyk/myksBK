package com.myks.myksbk.domain.work.dto;

import com.myks.myksbk.domain.work.domain.WorkMode;
import com.myks.myksbk.domain.work.domain.WorkStatus;

public record WorkCreateResponse(
        Long id,
        Long companyId,
        Long authorUserId,
        String title,
        String description,
        WorkMode mode,
        Boolean aiImageEnabled,
        WorkStatus status,
        String thumbnailUrl
) {}