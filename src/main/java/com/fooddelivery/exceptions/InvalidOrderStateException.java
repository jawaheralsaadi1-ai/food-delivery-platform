package com.fooddelivery.exceptions;

/**
 * Thrown when an order operation is attempted on an order whose current
 * status does not allow that operation.
 *
 * Examples:
 *   - Cancelling an order that is not PENDING
 *   - Adding items to a CONFIRMED order
 *   - Refunding a payment that is not COMPLETED
 *
 * Always maps to HTTP 409 Conflict.
 */
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