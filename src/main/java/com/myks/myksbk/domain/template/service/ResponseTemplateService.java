package com.myks.myksbk.domain.template.service;

import com.myks.myksbk.domain.template.domain.ResponseTemplate;
import com.myks.myksbk.domain.template.dto.ResponseTemplateDto;
import com.myks.myksbk.domain.template.repository.ResponseTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResponseTemplateService {

    private final ResponseTemplateRepository repository;

    // 목록 조회 (삭제 제외)
    @Transactional(readOnly = true)
    public List<ResponseTemplateDto.Response> getTemplates(Long companyId, String kind) {
        return repository.findAllByCompanyIdAndKindAndDeletedAtIsNullOrderByCreatedAtDesc(companyId, kind)
                .stream()
                .map(ResponseTemplateDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveTemplate(ResponseTemplateDto.SaveRequest request) {
        ResponseTemplate template = ResponseTemplate.builder()
                .companyId(request.getCompanyId())
                .kind(request.getKind())
                .title(request.getTitle())
                .prompt(request.getPrompt())
                .content(request.getContent())
                // .createdBy(userId) // 추후 로그인 연동 시 추가
                .build();

        repository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        ResponseTemplate template = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found id=" + id));

        template.markAsDeleted();
    }
}