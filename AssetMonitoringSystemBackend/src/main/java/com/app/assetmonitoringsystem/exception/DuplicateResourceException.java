package com.app.assetmonitoringsystem.exception;

/**
 * Exception thrown when a duplicate resource is detected (e.g., duplicate email).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
