package com.outside.api.controller;

public class CheckinRequest {
    private Long userId;
    private Long venueId;

    public Long getUserId() { 
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getVenueId() {
        return venueId;
    }
    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }
}