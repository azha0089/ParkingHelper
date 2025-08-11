package com.example.infomanagesystem.entity.vo;


import java.util.List;

/**
 * @Author forerunner
 * @Date 2025/4/10 21:01
 */

//泛型类
public class PageResult<T>{

    private final long total;
    private final long totalPage;
    private final long currentPage; //当前页面
    private final long pageSize;  //页面大小
    private final List<T> data;


    public PageResult(long currentPage, long pageSize, long total,List<T> data) {
        this.currentPage = currentPage;
        this.data = data;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPage = total % pageSize == 0L ? total / pageSize : total / pageSize + 1L;
    }

    public static <T> PageResult<T> of(long currentPage, long pageSize, long total,List<T> data) {
        if (currentPage < 0L) {
            throw new IllegalArgumentException("无效的页码数据<" + currentPage + ">");
        } else if (pageSize <= 0L) {
            throw new IllegalArgumentException("无效的每页显示数量<" + pageSize + ">");
        } else if (total < 0L) {
            throw new IllegalArgumentException("无效的总数据量<" + total + ">");
        } else {
            return new PageResult<T>(currentPage, pageSize, total, data);
        }
    }

    public List<T> getData() {
        return data;
    }

    public long getPageSize() {
        return pageSize;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public long getTotal() {
        return total;
    }
}
