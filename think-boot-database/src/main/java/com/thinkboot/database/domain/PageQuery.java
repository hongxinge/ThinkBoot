package com.thinkboot.database.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageQuery implements Serializable {

    private int current = 1;

    private int size = 10;

    private String orderBy;

    private String orderDirection;

    public boolean isAsc() {
        return !"desc".equalsIgnoreCase(orderDirection);
    }
}