package com.fooddelivery.exceptions;

/**
 * Thrown when an entity cannot be found by the given identifier,
 * OR when the entity exists but isActive = false (soft-deleted).
 * Both cases must surface as 404 — never 200 with stale data, never 403.
 */
public class ResourceNotFoundException extends RuntimeException {

    // 1. Free-form message — for email / code / custom lookups
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // 2. Standard "X with id N not found"
    public ResourceNotFoundException(String resourceName, Integer id) {
        super(resourceName + " with id " + id + " not found");
    }

    // 3. Generic field lookup — "X with <field> '<value>' not found"
    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(resourceName + " with " + field + " '" + value + "' not found");
    }
}