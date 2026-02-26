package com.myks.myksbk.domain.work.dto;

import com.myks.myksbk.domain.work.domain.WorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WorkCreateRequest(
        @NotNull Long companyId,
        @NotNull Long authorUserId,
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull WorkMode mode,
        @NotNull Boolean aiImageEnabled,
        List<String> tags
) {}