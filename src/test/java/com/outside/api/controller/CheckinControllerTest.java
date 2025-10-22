package com.outside.api.controller;

import com.outside.api.model.Checkin;
import com.outside.api.model.User;
import com.outside.api.model.Venue;
import com.outside.api.repository.CheckinRepository;
import com.outside.api.repository.UserRepository;
import com.outside.api.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CheckinController
 *
 * These tests verify that the checkin API endpoints work correctly end-to-end.
 * I'm using @SpringBootTest to load the full application context and MockMvc to
 * simulate HTTP requests without actually starting a server.
 *
 * @Transactional makes sure each test starts with a clean database state
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CheckinControllerTest {

    @Autowired
    private MockMvc mockMvc;  // Simulates HTTP requests to our API

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Test data that gets created fresh for each test
    private User testUser;
    private Venue testVenue;
    private Venue testVenue2;

    /**
     * Runs before each test to set up clean test data
     * This way every test starts with the same baseline data
     */
    @BeforeEach
    void setUp() {
        // Clear out any existing data to start fresh
        checkinRepository.deleteAll();
        userRepository.deleteAll();
        venueRepository.deleteAll();

        // Create a test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser = userRepository.save(testUser);

        // Create test venues
        testVenue = new Venue();
        testVenue.setName("Test Bar");
        testVenue.setLatitude(41.8781);
        testVenue.setLongitude(-87.6298);
        testVenue = venueRepository.save(testVenue);

        testVenue2 = new Venue();
        testVenue2.setName("Another Bar");
        testVenue2.setLatitude(41.8802);
        testVenue2.setLongitude(-87.6324);
        testVenue2 = venueRepository.save(testVenue2);
    }

    /**
     * Test that a user can successfully check in to a venue
     * Expected: HTTP 201 Created with checkin details
     */
    @Test
    void testCheckinUser_Success() throws Exception {
        // Build JSON request body with valid user and venue IDs
        String requestBody = String.format("{\"userId\": %d, \"venueId\": %d}",
                testUser.getId(), testVenue.getId());

        // Perform POST request and verify response
        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())  // Should return 201 Created
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.venueId").value(testVenue.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.venueName").value("Test Bar"))
                .andExpect(jsonPath("$.checkinTime").exists())
                .andExpect(jsonPath("$.checkoutTime").doesNotExist())  // Should be null since still checked in
                .andExpect(jsonPath("$.passwordHash").doesNotExist()); // Important: verify password isn't exposed!
    }

    /**
     * Test validation: request without userId should fail
     */
    @Test
    void testCheckinUser_MissingUserId() throws Exception {
        String requestBody = String.format("{\"venueId\": %d}", testVenue.getId());

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User ID is required"));
    }

    /**
     * Test validation: request without venueId should fail
     */
    @Test
    void testCheckinUser_MissingVenueId() throws Exception {
        String requestBody = String.format("{\"userId\": %d}", testUser.getId());

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Venue ID is required"));
    }

    /**
     * Test error handling: non-existent user should return error
     */
    @Test
    void testCheckinUser_InvalidUser() throws Exception {
        String requestBody = String.format("{\"userId\": 99999, \"venueId\": %d}", testVenue.getId());

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    /**
     * Test error handling: non-existent venue should return error
     */
    @Test
    void testCheckinUser_InvalidVenue() throws Exception {
        String requestBody = String.format("{\"userId\": %d, \"venueId\": 99999}", testUser.getId());

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Venue not found"));
    }

    @Test
    void testCheckinUser_AlreadyCheckedIn() throws Exception {
        // First checkin
        Checkin firstCheckin = new Checkin();
        firstCheckin.setUser(testUser);
        firstCheckin.setVenue(testVenue);
        firstCheckin.setCheckinTime(LocalDateTime.now());
        checkinRepository.save(firstCheckin);

        // Try to checkin again at same venue
        String requestBody = String.format("{\"userId\": %d, \"venueId\": %d}",
                testUser.getId(), testVenue.getId());

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User is already checked in to this venue"));
    }

    @Test
    void testCheckinUser_CooldownPeriod() throws Exception {
        // Create a recent checkout (5 minutes ago)
        Checkin recentCheckin = new Checkin();
        recentCheckin.setUser(testUser);
        recentCheckin.setVenue(testVenue);
        recentCheckin.setCheckinTime(LocalDateTime.now().minusMinutes(30));
        recentCheckin.setCheckoutTime(LocalDateTime.now().minusMinutes(5)); // 5 minutes ago
        checkinRepository.save(recentCheckin);

        // Try to checkin again (should fail - cooldown is 20 minutes)
        String requestBody = String.format("{\"userId\": %d, \"venueId\": %d}",
                testUser.getId(), testVenue.getId());

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Cooldown")));
    }

    @Test
    void testCheckinUser_DifferentVenue_AutoCheckout() throws Exception {
        // Checkin at first venue
        Checkin firstCheckin = new Checkin();
        firstCheckin.setUser(testUser);
        firstCheckin.setVenue(testVenue);
        firstCheckin.setCheckinTime(LocalDateTime.now());
        firstCheckin = checkinRepository.save(firstCheckin);

        // Checkin at different venue (should auto-checkout from first venue)
        String requestBody = String.format("{\"userId\": %d, \"venueId\": %d}",
                testUser.getId(), testVenue2.getId());

        mockMvc.perform(post("/api/checkins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.venueId").value(testVenue2.getId()));

        // Verify first checkin was auto-checked out
        Checkin updatedFirstCheckin = checkinRepository.findById(firstCheckin.getId()).get();
        assert updatedFirstCheckin.getCheckoutTime() != null;
    }

    @Test
    void testCheckoutUser_Success() throws Exception {
        // Create a checkin
        Checkin checkin = new Checkin();
        checkin.setUser(testUser);
        checkin.setVenue(testVenue);
        checkin.setCheckinTime(LocalDateTime.now());
        checkin = checkinRepository.save(checkin);

        mockMvc.perform(put("/api/checkins/" + checkin.getId() + "/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(checkin.getId()))
                .andExpect(jsonPath("$.checkoutTime").exists());
    }

    @Test
    void testCheckoutUser_NotFound() throws Exception {
        mockMvc.perform(put("/api/checkins/99999/checkout"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Checkin not found"));
    }

    @Test
    void testCheckoutUser_AlreadyCheckedOut() throws Exception {
        // Create a checkin that's already checked out
        Checkin checkin = new Checkin();
        checkin.setUser(testUser);
        checkin.setVenue(testVenue);
        checkin.setCheckinTime(LocalDateTime.now().minusHours(1));
        checkin.setCheckoutTime(LocalDateTime.now());
        checkin = checkinRepository.save(checkin);

        mockMvc.perform(put("/api/checkins/" + checkin.getId() + "/checkout"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User already checked out from this venue"));
    }

    @Test
    void testGetActiveCheckinsAtVenue() throws Exception {
        // Create active checkin
        Checkin activeCheckin = new Checkin();
        activeCheckin.setUser(testUser);
        activeCheckin.setVenue(testVenue);
        activeCheckin.setCheckinTime(LocalDateTime.now());
        checkinRepository.save(activeCheckin);

        // Create inactive (checked out) checkin
        Checkin inactiveCheckin = new Checkin();
        inactiveCheckin.setUser(testUser);
        inactiveCheckin.setVenue(testVenue);
        inactiveCheckin.setCheckinTime(LocalDateTime.now().minusHours(2));
        inactiveCheckin.setCheckoutTime(LocalDateTime.now().minusHours(1));
        checkinRepository.save(inactiveCheckin);

        mockMvc.perform(get("/api/checkins/venue/" + testVenue.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.checkins", hasSize(1)))
                .andExpect(jsonPath("$.checkins[0].checkoutTime").doesNotExist());
    }

    @Test
    void testGetUserCheckinHistory() throws Exception {
        // Create multiple checkins
        Checkin checkin1 = new Checkin();
        checkin1.setUser(testUser);
        checkin1.setVenue(testVenue);
        checkin1.setCheckinTime(LocalDateTime.now().minusHours(3));
        checkin1.setCheckoutTime(LocalDateTime.now().minusHours(2));
        checkinRepository.save(checkin1);

        Checkin checkin2 = new Checkin();
        checkin2.setUser(testUser);
        checkin2.setVenue(testVenue2);
        checkin2.setCheckinTime(LocalDateTime.now().minusHours(1));
        checkinRepository.save(checkin2);

        mockMvc.perform(get("/api/checkins/user/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId").value(testUser.getId()));
    }
}
