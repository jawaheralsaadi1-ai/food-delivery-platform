package com.fooddelivery.exceptions;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    // 2. Structured: "X with <field> '<value>' already exists"
    public DuplicateResourceException(String resourceName, String field, String value) {
        super(resourceName + " with " + field + " '" + value + "' already exists");
    }
}