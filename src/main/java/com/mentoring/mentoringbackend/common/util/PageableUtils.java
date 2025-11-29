package com.mentoring.mentoringbackend.common.util;

import com.mentoring.mentoringbackend.common.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageableUtils {

    private static final int MAX_PAGE_SIZE = 100;

    private PageableUtils() {
    }

    public static Pageable of(int page, int size) {
        int pageIndex = Math.max(0, page);
        int pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        return PageRequest.of(pageIndex, pageSize);
    }

    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return PageResponse.of(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
