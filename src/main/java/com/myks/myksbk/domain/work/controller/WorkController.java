package com.myks.myksbk.domain.work.controller;

import com.myks.myksbk.domain.work.dto.*;
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
            @AuthenticationPrincipal CustomUserPrincipal me,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.ok(workService.uploadThumbnail(workId, me.getId(), file));
    }

    @GetMapping(value = "/my", produces = "application/json")
    public ApiResponse<List<WorkSummaryResponse>> listMyWorks(@AuthenticationPrincipal CustomUserPrincipal me) {
        return ApiResponse.ok(workService.listMyWorks(me.getId()));
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ApiResponse<WorkDetailResponse> getWorkDetail(
            @PathVariable("id") Long workId,
            @AuthenticationPrincipal CustomUserPrincipal me
    ) {
        return ApiResponse.ok(workService.getWorkDetail(workId, me.getId()));
    }

    @GetMapping(value = "/{id}/episodes", produces = "application/json")
    public ApiResponse<List<EpisodeSummaryResponse>> listEpisodes(
            @PathVariable("id") Long workId,
            @AuthenticationPrincipal CustomUserPrincipal me
    ) {
        return ApiResponse.ok(workService.listEpisodes(workId, me.getId()));
    }

    @PostMapping(value = "/{id}/episodes", consumes = "application/json", produces = "application/json")
    public ApiResponse<Long> createEpisode(
            @PathVariable("id") Long workId,
            @AuthenticationPrincipal CustomUserPrincipal me,
            @RequestBody EpisodeSaveRequest req
    ) {
        return ApiResponse.ok(workService.createEpisode(workId, me.getId(), req));
    }

    @PutMapping(value = "/{id}/episodes/{episodeNo}", consumes = "application/json", produces = "application/json")
    public ApiResponse<Void> updateEpisode(
            @PathVariable("id") Long workId,
            @PathVariable("episodeNo") Integer episodeNo,
            @AuthenticationPrincipal CustomUserPrincipal me,
            @RequestBody EpisodeSaveRequest req
    ) {
        workService.updateEpisode(workId, episodeNo, me.getId(), req);
        return ApiResponse.ok(null);
    }
}