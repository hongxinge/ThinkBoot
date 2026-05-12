package com.thinkboot.web.result;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> records;

    private long total;

    private long current;

    private long size;

    private long pages;

    private boolean hasMore;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, long current, long size, long pages) {
        this.records = records != null ? records : Collections.emptyList();
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = pages;
        this.hasMore = this.size > 0 && current * size < total;
    }

    public PageResult(List<T> records, long total) {
        this.records = records != null ? records : Collections.emptyList();
        this.total = total;
        this.current = 1;
        this.size = this.records.size();
        this.pages = this.size > 0 ? (total + this.size - 1) / this.size : 0;
        this.hasMore = this.size > 0 && this.current * this.size < this.total;
    }

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records != null ? records : Collections.emptyList());
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages(size > 0 ? (total + size - 1) / size : 0);
        result.setHasMore(size > 0 && current * size < total);
        return result;
    }
}
