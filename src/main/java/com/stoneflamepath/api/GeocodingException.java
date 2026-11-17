package com.stoneflamepath.api;

/**
 * Thrown when a place name cannot be looked up because the map service failed.
 * The message is always safe to show to a user.
 */
public class GeocodingException extends Exception {

    /**
     * Creates the exception.
     *
     * @param userMessage a friendly explanation for the user
     * @param cause       the underlying technical problem
     */
    public GeocodingException(String userMessage, Throwable cause) {
        super(userMessage, cause);
    }

    /**
     * Creates the exception with no underlying cause.
     *
     * @param userMessage a friendly explanation for the user
     */
    public GeocodingException(String userMessage) {
        super(userMessage);
    }
}
