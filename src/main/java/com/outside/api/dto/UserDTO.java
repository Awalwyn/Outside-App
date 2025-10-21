package com.outside.api.dto;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) class for User entity
 *
 * User object w/ only non-sensitive fields
 *
 * Use Records b/c immutable (has no setters only getters // lombok creates setters so not immutable i.e. setEmail())
 */
public record UserDTO(Long id, String email, String username, String firstName, String lastName, LocalDateTime createdAt) {}
