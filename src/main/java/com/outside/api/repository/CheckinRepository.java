package com.outside.api.repository;
import com.outside.api.model.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

/**
 * Repository interface for Checkin entity
 * Handles database operations related to Checkins
 * 
 * All methods return queries related to Checkin records
 */

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {
    // Find all checkins by a specific user
    List<Checkin> findByUserIdAndCheckoutTimeIsNull(Long userId);

    //Find most recent checkin for a user at a specific venue
    Optional<Checkin> findFirstByUserIdAndVenueIdOrderByCheckinTimeDesc(Long userId, Long venueId);

    //Fine all active checkins at a specific venue
    List<Checkin> findByVenueIdAndCheckoutTimeIsNull(Long venueId);

    //Find all checkins for specific user (user's checkin history)
    List<Checkin> findByUserIdOrderByCheckinTimeDesc(Long userId);

    //Find all checkins that need to be checked out (i.e. checked in more than 2 hours ago)
    @Query("SELECT c FROM Checkin c WHERE c.checkoutTime IS NULL AND c.checkinTime < :twoHoursAgo")
    List<Checkin> findExpiredCheckins(@Param("twoHoursAgo") LocalDateTime twoHoursAgo);
}
