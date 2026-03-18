package com.myks.myksbk.domain.work.controller;

import com.myks.myksbk.domain.work.dto.EpisodeSummaryResponse;
import com.myks.myksbk.domain.work.dto.WorkCreateRequest;
import com.myks.myksbk.domain.work.dto.WorkCreateResponse;
import com.myks.myksbk.domain.work.dto.WorkSummaryResponse;
import com.myks.myksbk.domain.work.service.WorkService;
import com.myks.myksbk.global.api.ApiResponse;
import com.myks.myksbk.global.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/works")
public class WorkController {

    private final WorkService workService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ApiResponse<WorkCreateResponse> create(@Valid @RequestBody WorkCreateRequest req) {
        return ApiResponse.ok(workService.createWork(req));
    }

    @PostMapping(value = "/{id}/thumbnail", consumes = "multipart/form-data", produces = "application/json")
    public ApiResponse<String> uploadThumbnail(
            @PathVariable("id") Long workId,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(workService.uploadThumbnail(workId, file));
    }

    @GetMapping(value = "/my", produces = "application/json")
    public ApiResponse<List<WorkSummaryResponse>> listMyWorks(@AuthenticationPrincipal CustomUserPrincipal me) {
        return ApiResponse.ok(workService.listMyWorks(me.getId()));
    }

    @GetMapping(value = "/{id}/episodes", produces = "application/json")
    public ApiResponse<List<EpisodeSummaryResponse>> listEpisodes(
            @PathVariable("id") Long workId
    ) {
        return ApiResponse.ok(workService.listEpisodes(workId));
    }
}