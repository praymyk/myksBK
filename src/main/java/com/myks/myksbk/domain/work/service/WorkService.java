package com.myks.myksbk.domain.work.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myks.myksbk.domain.work.domain.Episode;
import com.myks.myksbk.domain.work.domain.Work;
import com.myks.myksbk.domain.work.domain.WorkStatus;
import com.myks.myksbk.domain.work.domain.EpisodeStatus;
import com.myks.myksbk.domain.work.repository.WorkRepository;
import com.myks.myksbk.domain.work.repository.EpisodeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.myks.myksbk.domain.work.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
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
    private final EpisodeRepository episodeRepository;
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
    public String uploadThumbnail(Long workId, Long requesterUserId, MultipartFile file) {
        Work work = validateOwnership(workId, requesterUserId);

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

    @Transactional(readOnly = true)
    public List<EpisodeSummaryResponse> listEpisodes(Long workId, Long requesterUserId) {
        validateOwnership(workId, requesterUserId);

        List<Episode> episodes = episodeRepository.findByWorkIdOrderByEpisodeNoAsc(workId);

        return episodes.stream()
                .map(ep -> new EpisodeSummaryResponse(
                        ep.getId(),
                        ep.getEpisodeNo(),
                        ep.getTitle(),
                        ep.getStatus().name()
                ))
                .toList();
    }

    @Transactional
    public Long createEpisode(Long workId, Long requesterUserId, EpisodeSaveRequest req) {
        validateOwnership(workId, requesterUserId);

        Integer lastNo = episodeRepository.findMaxEpisodeNoByWorkId(workId);
        int maxNo = (lastNo == null) ? 0 : lastNo;

        int finalEpisodeNo = (req.episodeNo() != null && req.episodeNo() > maxNo)
                ? req.episodeNo()
                : maxNo + 1;

        String paragraphsJson = null;
        String anchorsJson = null;
        try {
            if (req.paragraphs() != null) paragraphsJson = objectMapper.writeValueAsString(req.paragraphs());
            if (req.anchors() != null) anchorsJson = objectMapper.writeValueAsString(req.anchors());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 변환 실패", e);
        }

        Episode episode = Episode.builder()
                .workId(workId)
                .episodeNo(finalEpisodeNo)
                .title(req.title())
                .rawText(req.body())
                .paragraphsJson(paragraphsJson)
                .anchorsJson(anchorsJson)
                .status(req.status() != null ? req.status() : EpisodeStatus.DRAFT)
                .build();

        return episodeRepository.save(episode).getId();
    }

    @Transactional
    public void updateEpisodeDetail(Long workId, Long episodeId, Long requesterUserId, EpisodeSaveRequest req) {
        validateOwnership(workId, requesterUserId);

        Episode episode = episodeRepository.findByIdAndWorkId(episodeId, workId)
                .orElseThrow(() -> new EntityNotFoundException("에피소드를 찾을 수 없습니다."));

        String paragraphsJson = null;
        String anchorsJson = null;
        try {
            if (req.paragraphs() != null) paragraphsJson = objectMapper.writeValueAsString(req.paragraphs());
            if (req.anchors() != null) anchorsJson = objectMapper.writeValueAsString(req.anchors());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 변환 실패", e);
        }

        Episode updated = Episode.builder()
                .id(episode.getId())
                .workId(episode.getWorkId())
                .episodeNo(episode.getEpisodeNo())
                .title(req.title() != null ? req.title() : episode.getTitle())
                .rawText(req.body() != null ? req.body() : episode.getRawText())
                .paragraphsJson(req.paragraphs() != null ? paragraphsJson : episode.getParagraphsJson())
                .anchorsJson(req.anchors() != null ? anchorsJson : episode.getAnchorsJson())
                .status(req.status() != null ? req.status() : episode.getStatus())
                .createdAt(episode.getCreatedAt())
                .build();

        episodeRepository.save(updated);
    }

    @Transactional(readOnly = true)
    public EpisodeDetailResponse getEpisodeDetail(Long workId, Long episodeId, Long requesterUserId) {
        validateOwnership(workId, requesterUserId);

        Episode episode = episodeRepository.findByIdAndWorkId(episodeId, workId)
                .orElseThrow(() -> new EntityNotFoundException("에피소드를 찾을 수 없습니다."));

        List<String> paragraphs = List.of();
        List<AnchorDto> anchors = List.of();
        try {
            if (episode.getParagraphsJson() != null) {
                paragraphs = objectMapper.readValue(episode.getParagraphsJson(), new TypeReference<List<String>>() {});
            }
            if (episode.getAnchorsJson() != null) {
                anchors = objectMapper.readValue(episode.getAnchorsJson(), new TypeReference<List<AnchorDto>>() {});
            }
        } catch (JsonProcessingException e) {
            // 파싱 실패 시 빈 리스트 반환 (또는 예외 처리)
        }

        return new EpisodeDetailResponse(
                episode.getId(),
                episode.getWorkId(),
                episode.getEpisodeNo(),
                episode.getTitle(),
                episode.getRawText(),
                paragraphs,
                anchors,
                episode.getStatus(),
                episode.getCreatedAt(),
                episode.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public WorkDetailResponse getWorkDetail(Long workId, Long requesterUserId) {
        Work work = validateOwnership(workId, requesterUserId);

        return new WorkDetailResponse(
                work.getId(),
                work.getCompanyId(),
                work.getAuthorUserId(),
                work.getTitle(),
                work.getDescription(),
                work.getMode(),
                work.getAiImageEnabled(),
                work.getStatus(),
                work.getThumbnailUrl(),
                parseTags(work.getTagsJson()),
                work.getCreatedAt(),
                work.getUpdatedAt()
        );
    }

    public Work validateOwnership(Long workId, Long requesterUserId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new EntityNotFoundException("작품이 존재하지 않습니다."));

        if (!work.getAuthorUserId().equals(requesterUserId)) {
            throw new AccessDeniedException("본인의 작품만 접근 가능합니다.");
        }
        return work;
    }

    private List<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) return List.of();
        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}