package com.outside.api.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "venues")
@Data

public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    private String address;
    
    @Column (nullable = false)
    private Double latitude;

    @Column (nullable = false)
    private double longitude;
    private String category;
    private String phoneNumber;
    private String website;
    private Integer ageRestriction;
    private String coverCharge;


    @Column (columnDefinition = "TEXT")
    private String description;
    private String photoUrl;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt= LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
