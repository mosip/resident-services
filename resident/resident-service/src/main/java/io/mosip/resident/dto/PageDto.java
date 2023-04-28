package io.mosip.resident.dto;

import java.util.List;

/**
 * The Class PageDto.
 * @author Kamesh Shekhar Prasad
 *
 *
 * @param <T>
 */

public class PageDto<T> {
    private int pageNo;
    private int pageSize;
    private long totalItems;
    private int totalPages;
    private List<T> data;

    public int getPageNo() {
        return this.pageNo;
    }

    public int getPageSize() {
        return this.pageSize;
    }

    public long getTotalItems() {
        return this.totalItems;
    }

    public int getTotalPages() {
        return this.totalPages;
    }

    public List<T> getData() {
        return this.data;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setData(List<T> data) {
        this.data = data;
    }



    protected boolean canEqual(Object other) {
        return other instanceof PageDto;
    }

    public String toString() {
        int var10000 = this.getPageNo();
        return "PageDto(pageNo=" + var10000 + ", pageSize=" + this.getPageSize() + ", " + ", totalItems=" + this.getTotalItems() + ", totalPages=" + this.getTotalPages() + ", data=" + this.getData() + ")";
    }

    public PageDto(int pageNo, int pageSize, long totalItems, int totalPages, List<T> data) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.data = data;
    }

    public PageDto() {
    }
}

