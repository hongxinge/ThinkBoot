package com.thinkboot.database.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.regex.Pattern;

@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MAX_PAGE_SIZE = 500;

    private static final Pattern SAFE_COLUMN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private int current = 1;

    private int size = 10;

    private String orderBy;

    private String orderDirection;

    public void setCurrent(int current) {
        this.current = Math.max(1, current);
    }

    public void setSize(int size) {
        this.size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    }

    public void setOrderBy(String orderBy) {
        if (orderBy != null && !SAFE_COLUMN.matcher(orderBy).matches()) {
            throw new IllegalArgumentException("Invalid orderBy column name: " + orderBy);
        }
        this.orderBy = orderBy;
    }

    public void setOrderDirection(String orderDirection) {
        if (orderDirection != null
                && !"asc".equalsIgnoreCase(orderDirection)
                && !"desc".equalsIgnoreCase(orderDirection)) {
            throw new IllegalArgumentException("orderDirection must be 'asc' or 'desc'");
        }
        this.orderDirection = orderDirection;
    }

    public boolean isAsc() {
        return orderDirection == null || !"desc".equalsIgnoreCase(orderDirection);
    }
}