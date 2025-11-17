package com.zimono.trg.a.dto;

import java.util.List;

public class PaginatedResponse<T> {
    public int page;
    public int size;
    public long total;
    public List<T> items;

    public PaginatedResponse(int page, int size, long total, List<T> items) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.items = items;
    }
}

