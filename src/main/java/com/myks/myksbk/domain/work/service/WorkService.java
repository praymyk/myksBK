package com.myks.myksbk.domain.work.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myks.myksbk.domain.work.domain.WorkStatus;
import com.myks.myksbk.domain.work.dto.WorkCreateRequest;
import com.myks.myksbk.domain.work.dto.WorkCreateResponse;
import com.myks.myksbk.domain.work.domain.Work;
import com.myks.myksbk.domain.work.dto.WorkSummaryResponse;
import com.myks.myksbk.domain.work.repository.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.work-thumbnail-dir}")
    private String thumbnailDir;

    @Value("${app.public.base-url}")
    private String publicBaseUrl;

    @Transactional
    public WorkCreateResponse createWork(WorkCreateRequest req) {
        String tagsJson = null;
        if (req.tags() != null && !req.tags().isEmpty()) {
            try {
                tagsJson = objectMapper.writeValueAsString(req.tags());
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("tags 변환 실패");
            }
        }

        Work work = Work.builder()
                .companyId(req.companyId())
                .authorUserId(req.authorUserId())
                .title(req.title())
                .description(req.description())
                .mode(req.mode())
                .aiImageEnabled(req.aiImageEnabled())
                .status(WorkStatus.DRAFT)
                .tagsJson(tagsJson)
                .build();

        Work saved = workRepository.save(work);

        return new WorkCreateResponse(
                saved.getId(),
                saved.getCompanyId(),
                saved.getAuthorUserId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getMode(),
                saved.getAiImageEnabled(),
                saved.getStatus(),
                saved.getThumbnailUrl()
        );
    }

    @Transactional
    public String uploadThumbnail(Long workId, MultipartFile file) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new IllegalArgumentException("작품이 존재하지 않습니다."));

        if (file.isEmpty()) throw new IllegalArgumentException("파일이 비어있습니다.");
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");

        try {
            Files.createDirectories(Paths.get(thumbnailDir));

            String ext = guessExt(file.getOriginalFilename());
            String key = workId + "/" + UUID.randomUUID() + ext; // 저장용 key
            Path dest = Paths.get(thumbnailDir).resolve(key);

            Files.createDirectories(dest.getParent());
            file.transferTo(dest.toFile());

            // 정적 서빙 URL (/uploads/** 로 노출)
            String url = publicBaseUrl + "/uploads/" + key;

            work.setThumbnailKey(key);
            work.setThumbnailUrl(url);

            return url;
        } catch (Exception e) {
            throw new IllegalStateException("썸네일 업로드 실패", e);
        }
    }

    private String guessExt(String filename) {
        if (filename == null) return ".jpg";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return ".png";
        if (lower.endsWith(".webp")) return ".webp";
        if (lower.endsWith(".gif")) return ".gif";
        return ".jpg";
    }

    @Transactional(readOnly = true)
    public List<WorkSummaryResponse> listMyWorks(Long authorUserId) {
        List<Work> works = workRepository.findByAuthorUserIdOrderByUpdatedAtDesc(authorUserId);

        return works.stream()
                .map(w -> new WorkSummaryResponse(
                        w.getId(),
                        w.getTitle(),
                        w.getMode(),
                        w.getStatus(),
                        w.getThumbnailUrl(),
                        w.getCreatedAt(),
                        w.getUpdatedAt()
                ))
                .toList();
    }
}