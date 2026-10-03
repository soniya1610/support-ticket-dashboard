package com.example.tickets.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Page numbers are 1-based in the API. */
public record PagedResponse<T>(List<T> data, int page, int pageSize, long total, int totalPages) {

    public static <T> PagedResponse<T> from(Page<T> p) {
        return new PagedResponse<>(p.getContent(), p.getNumber() + 1, p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}
