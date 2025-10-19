package com.outside.api.controller;

import com.outside.api.model.Venue;
import com.outside.api.service.VenueService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController 
@RequestMapping("/api/venues")
@CrossOrigin(origins= "*")

public class VenueController {
    @Autowired
    private VenueService venueService;

    //All Venues - API request
    @GetMapping
    public ResponseEntity<List<Venue>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    } 

    //Venue by ID - API request
    @GetMapping("/{id}")
    public ResponseEntity<Venue> getVenueById(@PathVariable Long id) {
        return venueService.getVenuebyId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    } 

    //Create Venue - API request
    @PostMapping
    public ResponseEntity<Venue> createVenue(@RequestBody Venue venue) {
        Venue createdVenue = venueService.createVenue(venue);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVenue);
    } 

    //Update Venue - API request
    @PutMapping("/{id}")
    public ResponseEntity<Venue> updateVenue(@PathVariable Long id, @RequestBody Venue venueDetails) {
        try {
            Venue updated = venueService.updateVenue(id, venueDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    } 

    //Nearby Venues - API request
    @GetMapping("/nearby")
    public ResponseEntity<List<Venue>> getVenuesNearby(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double radiusMi) {
        List<Venue> venues = venueService.getVenuesNearby(lat, lon, radiusMi);
        return ResponseEntity.ok(venues);
    } 

    //Venues by Category - API request
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Venue>> getVenuesByCategory(@PathVariable String category) {
        List<Venue> venues = venueService.getVenuesByCategory(category);
        return ResponseEntity.ok(venues);
    } 

    //Search Venues - API request
    @GetMapping("/search")
    public ResponseEntity<List<Venue>> searchVenues(@RequestParam String query) {
        List<Venue> venues = venueService.searchVenues(query);
        return ResponseEntity.ok(venues);
    } 

    //Delete Venue - API request
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();  
    }
}
