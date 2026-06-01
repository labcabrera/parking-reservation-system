package org.labcabrera.parking.catalog.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SearchRequest {

    private String q;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private int page = 0;
    private int size = 20;
    private Double lat;
    private Double lng;
    private Double radiusKm;
    private List<String> features;

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

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Double getRadiusKm() { return radiusKm; }
    public void setRadiusKm(Double radiusKm) { this.radiusKm = radiusKm; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
}
