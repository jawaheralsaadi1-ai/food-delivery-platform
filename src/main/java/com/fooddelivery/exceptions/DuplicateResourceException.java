package com.fooddelivery.exceptions;

/**
 * Thrown when a create/update operation would violate a uniqueness constraint
 * against an existing, still-active record.
 *
 * Examples:
 *   - Registering an email that already exists on an active Customer
 *   - Creating a second Payment for the same Order
 *
 * Always maps to HTTP 409 Conflict.
 * NOTE: only conflicts with *active* records count; a soft-deleted record
 * with the same email does NOT block re-registration.
 */
public class DuplicateResourceException extends RuntimeException {

    // 1. Free-form message
    public DuplicateResourceException(String message) {
        super(message);
    }

    // 2. Structured: "X with <field> '<value>' already exists"
    public DuplicateResourceException(String resourceName, String field, String value) {
        super(resourceName + " with " + field + " '" + value + "' already exists");
    }
}