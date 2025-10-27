package com.outside.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.outside.api.model.Venue;

import java.util.List;


@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByCategory(String category);

    @Query("SELECT v FROM Venue v WHERE v.latitude BETWEEN :minLat AND :maxLat AND v.longitude BETWEEN :minLon AND :maxLon")
        List<Venue> findVenuesInBounds(double minLat, double maxLat, double minLon, double maxLon);

    @Query("SELECT v FROM Venue v WHERE LOWER(v.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    List<Venue> searchByName(String namePart);
}
