package org.labcabrera.parking.catalog.application.dto;

import java.time.LocalDateTime;

public class SearchRequest {

    private String q;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private int page = 0;
    private int size = 20;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public LocalDateTime getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDateTime checkIn) { this.checkIn = checkIn; }

    public LocalDateTime getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDateTime checkOut) { this.checkOut = checkOut; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
