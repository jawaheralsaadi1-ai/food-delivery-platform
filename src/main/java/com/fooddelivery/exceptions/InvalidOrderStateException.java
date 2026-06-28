package com.fooddelivery.exceptions;

public class InvalidOrderStateException extends RuntimeException {

    private final String currentState;
    private final String attemptedAction;

    public InvalidOrderStateException(String currentState, String attemptedAction) {
        super(String.format("Cannot %s order in state: %s", attemptedAction, currentState));
        this.currentState = currentState;
        this.attemptedAction = attemptedAction;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getAttemptedAction() {
        return attemptedAction;
    }
}