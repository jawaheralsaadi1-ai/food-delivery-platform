package com.fooddelivery.exceptions;

public class InvalidOrderStateException extends RuntimeException {

    // 1. Free-form message
    public InvalidOrderStateException(String message) {
        super(message);
    }

    // 2. Structured: tells exactly what state was expected vs found
    public InvalidOrderStateException(String resource, Integer id,
                                      String currentStatus, String requiredStatus) {
        super(resource + " " + id + " cannot be processed: "
                + "current status is '" + currentStatus + "' "
                + "but '" + requiredStatus + "' is required");
    }
}