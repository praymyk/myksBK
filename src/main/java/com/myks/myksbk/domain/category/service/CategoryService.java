package com.myks.myksbk.domain.category.service;

import com.myks.myksbk.domain.category.domain.Category;
import com.myks.myksbk.domain.category.domain.CategoryKind;
import com.myks.myksbk.domain.category.dto.CategoryDto;
import com.myks.myksbk.domain.category.repository.CategoryKindRepository;
import com.myks.myksbk.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryKindRepository categoryKindRepository;

    // 1. 페이지 초기 데이터 조회
    public CategoryDto.PageResponse getCategoryPageData(Long companyId) {
        // Kinds 조회
        List<CategoryKind> kinds = categoryKindRepository.findAllByIsActiveTrueOrderByIdAsc();

        // Categories 조회
        List<Category> categories = categoryRepository.findAllByCompanyIdAndIsActiveTrueOrderByLevelAscSortOrderAsc(companyId);

        return CategoryDto.PageResponse.builder()
                .companyId(companyId)
                .kinds(kinds.stream().map(this::toKindResponse).collect(Collectors.toList()))
                .categories(categories.stream().map(this::toCategoryResponse).collect(Collectors.toList()))
                .build();
    }

    // 2. 트리 저장
    @Transactional
    public void replaceCategoryTree(CategoryDto.TreeSaveRequest request) {
        Long companyId = request.getCompanyId();
        String kindCode = request.getKind();
        List<CategoryDto.TreeSaveRequest.Node> inputNodes = request.getNodes();

        // 1) Kind 조회
        CategoryKind kind = categoryKindRepository.findByCode(kindCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid kind code: " + kindCode));

        // 2) 기존 카테고리 로드
        List<Category> existingList = categoryRepository.findAllByCompanyIdAndKind(companyId, kind);
        Map<Long, Category> existingMap = existingList.stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        // 3) 요청에 포함된 기존 ID 집합
        Set<Long> payloadIds = inputNodes.stream()
                .map(CategoryDto.TreeSaveRequest.Node::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4) 삭제 대상 처리 (Soft Delete) -> 요청에 없는 기존 ID들은 비활성화
        for (Category existing : existingList) {
            if (!payloadIds.contains(existing.getId())) {
                existing.deactivate(); // is_active = 0
            }
        }

        // 5) ClientId -> DB ID 매핑 맵 (신규 노드의 부모 연결용)
        Map<Long, Long> clientToDbId = new HashMap<>();

        // 기존 노드들의 ID 매핑 미리 등록
        for (CategoryDto.TreeSaveRequest.Node node : inputNodes) {
            if (node.getId() != null) {
                clientToDbId.put(node.getClientId(), node.getId());
            }
        }

        // 6) 저장/업데이트 처리
        // Level 순서대로 정렬해야 부모가 먼저 생성됨 (Node.js의 while 루프 대신 정렬 사용)
        inputNodes.sort(Comparator.comparingInt(CategoryDto.TreeSaveRequest.Node::getLevel)
                .thenComparingInt(CategoryDto.TreeSaveRequest.Node::getSortOrder));

        for (CategoryDto.TreeSaveRequest.Node node : inputNodes) {
            Category parent = null;
            if (node.getParentClientId() != null) {
                Long parentDbId = clientToDbId.get(node.getParentClientId());
                if (parentDbId != null) {
                    // 부모 엔티티 프록시 조회 (쿼리 절약)
                    parent = categoryRepository.getReferenceById(parentDbId);
                }
            }

            if (node.getId() != null) {
                // [Update] 기존 노드
                Category existing = existingMap.get(node.getId());
                if (existing != null) {
                    existing.update(node.getName(), node.getLevel(), node.getSortOrder(), node.getActive(), parent);
                }
            } else {
                // [Insert] 신규 노드
                Category newCategory = Category.builder()
                        .companyId(companyId)
                        .kind(kind)
                        .parent(parent)
                        .name(node.getName())
                        .level(node.getLevel())
                        .sortOrder(node.getSortOrder())
                        .isActive(node.getActive())
                        .build();

                categoryRepository.save(newCategory);
                // 중요: 방금 생성된 ID를 매핑에 추가해야 자식들이 참조 가능
                clientToDbId.put(node.getClientId(), newCategory.getId());
            }
        }
    }

    // Helpers
    private CategoryDto.CategoryKindResponse toKindResponse(CategoryKind k) {
        return CategoryDto.CategoryKindResponse.builder()
                .id(k.getId())
                .code(k.getCode())
                .name(k.getName())
                .build();
    }

    private CategoryDto.CategoryResponse toCategoryResponse(Category c) {
        return CategoryDto.CategoryResponse.builder()
                .id(c.getId())
                .kindId(c.getKind().getId())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .level(c.getLevel())
                .name(c.getName())
                .sortOrder(c.getSortOrder())
                .isActive(c.getIsActive())
                .build();
    }
}