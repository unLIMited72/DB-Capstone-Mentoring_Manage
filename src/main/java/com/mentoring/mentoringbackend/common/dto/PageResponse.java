package com.mentoring.mentoringbackend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

// 🔹 이거 추가
import org.springframework.data.domain.Page;

@Getter
@Builder
@AllArgsConstructor
public class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;

    public static <T> PageResponse<T> of(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(last)
                .build();
    }

    // 🔹 새로 추가: Page<?>를 그대로 받는 버전
    public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
        return of(
                content,
                page.getNumber(),        // 현재 페이지 (0-based)
                page.getSize(),          // 페이지 크기
                page.getTotalElements(), // 전체 row 수
                page.getTotalPages(),    // 전체 페이지 수
                page.isLast()            // 마지막 페이지 여부
        );
    }
}
