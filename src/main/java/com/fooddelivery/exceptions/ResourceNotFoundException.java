package com.fooddelivery.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String resource, Integer id) {
        super(resource + " with id " + id + " not found");
    }
}
