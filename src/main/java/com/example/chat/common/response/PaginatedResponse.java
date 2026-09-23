package com.example.chat.common.response;

import lombok.Getter;

import java.util.List;

@Getter
public class PaginatedResponse<T> {
    private final boolean success;
    private final List<T> data;
    private final Pagination pagination;
    private final String message;

    private PaginatedResponse(List<T> data, Pagination pagination) {
        this.success = true;
        this.data = data;
        this.pagination = pagination;
        this.message = null;
    }

    public static <T> PaginatedResponse<T> of(List<T> data, boolean hasMore, Long nextCursor) {
        return new PaginatedResponse<>(data, new Pagination(hasMore, nextCursor));
    }

    @Getter
    public static class Pagination {
        private final boolean hasMore;
        private final Long nextCursor;

        public Pagination(boolean hasMore, Long nextCursor) {
            this.hasMore = hasMore;
            this.nextCursor = nextCursor;
        }
    }
}
