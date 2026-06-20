package com.ticket.support_management_system_api.common.utils;

import com.ticket.support_management_system_api.common.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.function.Function;

public class PaginationUtils {

    private PaginationUtils() {}

    public static <S, T> PageResponse<T> toPageResponse(Page<S> page, Function<S, T> mapper) {
        return PageResponse.<T>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
