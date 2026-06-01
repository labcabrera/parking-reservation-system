package org.labcabrera.parking.catalog.application.dto;

import java.util.List;

public class SearchResponse {

    private String searchSessionId;
    private boolean stale;
    private List<FacilityResult> content;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;

    public SearchResponse() {}

    public SearchResponse(String searchSessionId, boolean stale,
                          List<FacilityResult> content, int page, int size,
                          int totalPages, long totalElements) {
        this.searchSessionId = searchSessionId;
        this.stale = stale;
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

    public String getSearchSessionId() { return searchSessionId; }
    public void setSearchSessionId(String searchSessionId) { this.searchSessionId = searchSessionId; }

    public boolean isStale() { return stale; }
    public void setStale(boolean stale) { this.stale = stale; }

    public List<FacilityResult> getContent() { return content; }
    public void setContent(List<FacilityResult> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
}
