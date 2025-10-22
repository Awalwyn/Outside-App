package com.outside.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Checkin Entity
 * 
 * Represents a user's check-in at a venue.
 * 
 * Key concepts:
 * - One user can have many check-ins (one-to-many relationship with User)
 * - One venue can have many check-ins (one-to-many relationship with Venue)
 * - checkoutTime is nullable: if null, user is currently checked in
 * - createdAt is immutable (set on creation only, never updated)
 */
@Entity
@Table(name = "checkins")
@Data
public class Checkin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Foreign key to User entity
     * 
     * @ManyToOne relationship: many check-ins can belong to one user
     * nullable=false: a check-in must always be associated with a user
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Foreign key to Venue entity
     * 
     * @ManyToOne relationship: many check-ins can belong to one venue
     * nullable=false: a check-in must always be associated with a venue
     */
    @ManyToOne
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
    
    /**
     * Timestamp when the user checked in
     * 
     * nullable=false: every check-in must have a check-in time
     * updatable=false: once set, this never changes (immutable)
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime checkinTime;
    
    /**
     * Timestamp when the user checked out
     * 
     * Nullable by design: if null, user is currently checked in
     * If has a value: user has checked out (either manually or auto-checkout)
     * 
     * Set by:
     * 1. Manual checkout (user calls checkout endpoint)
     * 2. Auto-checkout (scheduled job runs after 2 hours)
     * 3. Auto-checkout when user checks into different venue
     */
    @Column
    private LocalDateTime checkoutTime;
    
    /**
     * Metadata: when this record was created in the database
     * 
     * nullable=false: must have a creation time
     * updatable=false: never changes after creation (audit trail)
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Metadata: when this record was last updated
     * 
     * Updated whenever the record changes (e.g., when checkoutTime is set)
     * Used to track when fields like checkoutTime were modified
     */
    @Column
    private LocalDateTime updatedAt;
    
    /**
     * JPA lifecycle hook: runs automatically before INSERT
     * 
     * Sets createdAt and updatedAt to current time.
     * Called by Hibernate before inserting a new Checkin record.
     * Ensures every record has a creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * JPA lifecycle hook: runs automatically before UPDATE
     * 
     * Updates updatedAt to current time whenever the record changes.
     * Called by Hibernate before updating an existing Checkin record.
     * Keeps an audit trail of when fields were last modified.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}