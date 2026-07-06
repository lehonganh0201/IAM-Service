package com.example.commonlib.api.common;

import com.example.commonlib.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * ----------------------------------------------------------------------------
 * Author:        Hong Anh
 * Created on:    17/06/2026 at 16:20
 * Project:       IAMService
 * Contact:       https://github.com/lehonganh0201
 * ----------------------------------------------------------------------------
 */

@Component
public class PageableFactory {

    private static final int MAX_PAGE_SIZE = 100;

    public Pageable create(
            int page,
            int size,
            String sortBy,
            String sortDir,
            Set<String> allowedSortFields
    ) {
        if (page < 0) {
            throw new BadRequestException("Page index must not be negative");
        }

        if (size <= 0) {
            throw new BadRequestException("Page size must be greater than 0");
        }

        if (size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + MAX_PAGE_SIZE);
        }

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }

        if (!allowedSortFields.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy);
        }

        Sort.Direction direction = resolveDirection(sortDir);

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );
    }

    private Sort.Direction resolveDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return Sort.Direction.DESC;
        }

        try {
            return Sort.Direction.fromString(sortDir);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid sort direction: " + sortDir);
        }
    }
}
