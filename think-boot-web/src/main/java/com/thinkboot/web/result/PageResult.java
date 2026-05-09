package com.thinkboot.web.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {

    private List<T> records;

    private long total;

    private long current;

    private long size;

    private long pages;

    public PageResult() {
    }

    public PageResult(List<T> records, long total, long current, long size, long pages) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = pages;
    }

    public PageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
        this.current = 1;
        this.size = records != null ? records.size() : 0;
        this.pages = this.size > 0 ? (total + this.size - 1) / this.size : 0;
    }

    public static <T> PageResult<T> of(List<T> records, long total, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages(size > 0 ? (total + size - 1) / size : 0);
        return result;
    }
}