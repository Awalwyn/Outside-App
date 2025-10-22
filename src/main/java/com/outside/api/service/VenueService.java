package com.outside.api.service;

import com.outside.api.model.Venue;
import com.outside.api.repository.VenueRepository;

//import org.hibernate.annotations.TimeZoneStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VenueService {
    @Autowired
    private VenueRepository venueRepository;

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();

    }

    public Optional<Venue> getVenuebyId(Long id) {
        return venueRepository.findById(id);
    }

    public Venue createVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    public Venue updateVenue(Long id, Venue venueDetails) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
        venue.setName(venueDetails.getName());
        venue.setAddress(venueDetails.getAddress());
        venue.setLatitude(venueDetails.getLatitude());
        venue.setLongitude(venueDetails.getLongitude());
        venue.setCategory(venueDetails.getCategory());
        venue.setPhoneNumber(venueDetails.getPhoneNumber());
        venue.setWebsite(venueDetails.getWebsite());
        venue.setAgeRestriction(venueDetails.getAgeRestriction());
        venue.setCoverCharge(venueDetails.getCoverCharge());
        venue.setDescription(venueDetails.getDescription());
        venue.setPhotoUrl(venueDetails.getPhotoUrl());

        return venueRepository.save(venue);
    }

    public List<Venue> getVenuesByCategory(String category) {
        return venueRepository.findByCategory(category);
    }

    //bounding box calculation for nearby venues
    public List<Venue> getVenuesNearby(Double lat, Double lon, Double radiusMi) {
        Double latDiff = radiusMi / 69.0;
        Double lonDiff = radiusMi / (69.0 * Math.cos(Math.toRadians(lat)));

        return venueRepository.findVenuesInBounds(lat-latDiff, lat+latDiff,lon-lonDiff, lon+lonDiff);
    }

    public List<Venue> searchVenues(String query) {
        return venueRepository.searchByName(query);
    }

    //change to soft delete later to preserve data integrity
    public void deleteVenue(Long id) {
        venueRepository.deleteById(id);
    }


}
