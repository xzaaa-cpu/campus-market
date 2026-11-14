package com.campus.market.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果包装。
 */
@Data
public class PageResult<T> {

    private long total;
    private long page;
    private long size;
    private List<T> list;

    public PageResult(long total, long page, long size, List<T> list) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.list = list;
    }
}
