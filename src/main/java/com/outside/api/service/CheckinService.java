package com.outside.api.service;

import com.outside.api.model.Checkin;
import com.outside.api.model.User;
import com.outside.api.model.Venue;
import com.outside.api.repository.CheckinRepository;
import com.outside.api.repository.UserRepository;
import com.outside.api.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CheckinService {
    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    private static final int AUTO_CHECKOUT_HOURS = 2;
    private static final int COOLDOWN_MINUTES = 20;

    /**
     * User checks in to a venue (validate user, validate venue, check cooldown,
     * auto checkout, create new checkin)
     * 
     * @param userId  ID of the user checking in
     * @param venueId ID of the venue to check in to
     * @return The created Checkin record
     * @throws RuntimeException if user or venue not found, or if user is in
     * cooldown period
     */
    @Transactional
    public Checkin checkinUser(Long userId, Long venueId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        //Venue validation
        Venue venue = venueRepository.findById(venueId).orElseThrow(() -> new RuntimeException("Venue not found"));

        //check cooldown
        Optional<Checkin> lastCheckin = checkinRepository.findFirstByUserIdAndVenueIdOrderByCheckinTimeDesc(userId, venueId);

        if(lastCheckin.isPresent()) {
            Checkin previous = lastCheckin.get();

            if (previous.getCheckoutTime() == null) {
                throw new RuntimeException("User is already checked in to this venue");
            }

            //Calculate time since last checkout
            LocalDateTime lastCheckout = previous.getCheckoutTime();
            long minutesAgo = ChronoUnit.MINUTES.between(lastCheckout, LocalDateTime.now());

            //if last w/i cooldown period, throw error
            if (minutesAgo < COOLDOWN_MINUTES) {
                throw new RuntimeException("User is in Cooldown. Try again in " + (COOLDOWN_MINUTES - minutesAgo) + " minutes.");
            }
        }

        //Auto checkout any expired checkins for this user
        List<Checkin> activeCheckins = checkinRepository.findByUserIdAndCheckoutTimeIsNull(userId);
        for (Checkin checkin:activeCheckins) {
            checkin.setCheckoutTime(LocalDateTime.now());
            checkinRepository.save(checkin);
        }

        //create new checking
        Checkin newCheckin = new Checkin();
        newCheckin.setUser(user);
        newCheckin.setVenue(venue);
        newCheckin.setCheckinTime(LocalDateTime.now());
        return checkinRepository.save(newCheckin);
    }

    /**
     * Manual User checkout from a venue
     * 
     * @param checkinId ID of the checkin record to checkout
     * @return The updated Checkin record with checkoutTime set
     * @throws RuntimeException if checkin not found or already checked out
     */
    @Transactional
    public Checkin checkoutUser(Long checkinId) {
        Checkin checkin = checkinRepository.findById(checkinId)
                .orElseThrow(() -> new RuntimeException("Checkin not found"));
        
        if (checkin.getCheckoutTime() != null) {
            throw new RuntimeException("User already checked out from this venue");
        }

        checkin.setCheckoutTime(LocalDateTime.now());
        return checkinRepository.save(checkin);
    }

    /**
     * Get currently active checkins for a venue
     * 
     * @param venueId ID of the venue
     * @return List of active Checkin records for the venue
     */

    public List<Checkin> getActiveCheckinsForVenue(Long venueId) {
        return checkinRepository.findByVenueIdAndCheckoutTimeIsNull(venueId);
    }

    /**
     * Get checkin history for a user
     * 
     * @param userId ID of the user
     * @return List of Checkin records for the user
     */
    public List<Checkin> getCheckinHistoryForUser(Long userId) {
        return checkinRepository.findByUserIdOrderByCheckinTimeDesc(userId);
    }

    /**
     * Scheduled task to auto-checkout users who have been checked in for more than
     * AUTO_CHECKOUT_HOURS
     * Runs every hour
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) //every hour
    @Transactional
    public void autoCheckoutExpiredCheckins() {
        //calculate 2 hours ago
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(AUTO_CHECKOUT_HOURS);

        //find allexpired checkins (older than 2 hours && checkoutTime is null)
        List<Checkin> expiredCheckins = checkinRepository.findExpiredCheckins(twoHoursAgo);
        
        //checkout each one
        for (Checkin checkin : expiredCheckins) {
            checkin.setCheckoutTime(LocalDateTime.now());
            checkinRepository.save(checkin);
        }
    }

    public void deleteCheckin(Long checkinId) {
    Checkin checkin = checkinRepository.findById(checkinId)
        .orElseThrow(() -> new RuntimeException("Checkin not found"));
    
    checkinRepository.delete(checkin);
}
}
