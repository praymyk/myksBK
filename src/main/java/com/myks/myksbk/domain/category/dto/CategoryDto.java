package com.myks.myksbk.domain.category.dto;

import lombok.*;
import java.util.List;

public class CategoryDto {

    @Getter @Builder
    public static class CategoryKindResponse {
        private Long id;
        private String code;
        private String name;
    }

    @Getter @Builder
    public static class CategoryResponse {
        private Long id;
        private Long kindId;
        private Long parentId;
        private Integer level;
        private String name;
        private Integer sortOrder;
        private Boolean isActive;
    }

    // 초기 페이지 로드용 복합 응답
    @Getter @Builder
    public static class PageResponse {
        private Long companyId;
        private List<CategoryKindResponse> kinds;
        private List<CategoryResponse> categories;
    }

    // 트리 저장 요청 DTO
    @Getter @Setter @NoArgsConstructor
    public static class TreeSaveRequest {
        private Long companyId;
        private String kind;
        private List<Node> nodes;

        @Getter @Setter @NoArgsConstructor
        public static class Node {
            private Long id;
            private Long clientId;
            private Long parentClientId;
            private Integer level;
            private String name;
            private Integer sortOrder;
            private Boolean active;
        }
    }
}