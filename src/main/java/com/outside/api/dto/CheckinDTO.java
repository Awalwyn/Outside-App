package com.outside.api.dto;

import java.time.LocalDateTime;

/**
 * DTO for Checkin responses
 * Excludes sensitive user data like password hashes
 */
public record CheckinDTO(
    Long id,
    Long userId,
    String username,
    Long venueId,
    String venueName,
    LocalDateTime checkinTime,
    LocalDateTime checkoutTime,
    LocalDateTime createdAt
) {}