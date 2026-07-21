package com.example.commonlib.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 16:03
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        SortResponse sort
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                SortResponse.from(page.getSort())
        );
    }

    public record SortResponse(
            String property,
            String direction
    ) {

        public static SortResponse from(Sort sort) {
            if (sort == null || sort.isUnsorted()) {
                return null;
            }

            Sort.Order order = sort.stream().findFirst().orElse(null);

            if (order == null) {
                return null;
            }

            return new SortResponse(
                    order.getProperty(),
                    order.getDirection().name()
            );
        }
    }
}