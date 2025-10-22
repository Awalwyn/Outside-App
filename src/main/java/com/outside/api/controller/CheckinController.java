package com.outside.api.controller;

import com.outside.api.dto.CheckinDTO;
import com.outside.api.model.Checkin;
import com.outside.api.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/checkins")
@CrossOrigin(origins = "*")

public class CheckinController {
    @Autowired
    private CheckinService checkinService;

    @PostMapping
    public ResponseEntity<?> checkinUser (@RequestBody CheckinRequest request) {
        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User ID is required"));

        }

        if(request.getVenueId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Venue ID is required"));
        }

        try {
            Checkin checkin = checkinService.checkinUser(request.getUserId(), request.getVenueId());
            CheckinDTO checkinDTO = convertToDTO(checkin);
            return ResponseEntity.status(HttpStatus.CREATED).body(checkinDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{id}/checkout")
    public ResponseEntity<?> checkoutUser (@PathVariable Long id) {
        try {
            Checkin checkin = checkinService.checkoutUser(id);
            CheckinDTO checkinDTO = convertToDTO(checkin);
            return ResponseEntity.ok(checkinDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<?> getActiveCheckinsByVenue(@PathVariable Long venueId) {
        List<Checkin> checkins = checkinService.getActiveCheckinsForVenue(venueId);
        List<CheckinDTO> checkinDTOs = checkins.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("count", checkinDTOs.size(), "checkins", checkinDTOs));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserCheckinHistory(@PathVariable Long userId) {
        List<Checkin> checkins = checkinService.getCheckinHistoryForUser(userId);
        List<CheckinDTO> checkinDTOs = checkins.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(checkinDTOs);
    }

    private CheckinDTO convertToDTO(Checkin checkin) {
        return new CheckinDTO(
            checkin.getId(),
            checkin.getUser().getId(),
            checkin.getUser().getUsername(),
            checkin.getVenue().getId(),
            checkin.getVenue().getName(),
            checkin.getCheckinTime(),
            checkin.getCheckoutTime(),
            checkin.getCreatedAt()
        );
    }
    
}


