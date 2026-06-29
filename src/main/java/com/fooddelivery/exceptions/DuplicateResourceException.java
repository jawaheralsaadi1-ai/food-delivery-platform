package com.fooddelivery.exceptions;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String entityName, String field, String value) {
        super(entityName + " already exists with " + field + ": " + value);
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}