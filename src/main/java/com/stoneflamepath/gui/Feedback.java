package com.stoneflamepath.gui;

/**
 * A short message for the status line after a button press.
 *
 * @param message the text to show
 * @param failed  true if the action did not go through
 */
public record Feedback(String message, boolean failed) {

    /**
     * Creates a success message.
     *
     * @param message the text to show
     * @return a feedback that is not a failure
     */
    public static Feedback success(String message) {
        return new Feedback(message, false);
    }

    /**
     * Creates a failure message.
     *
     * @param message the text to show
     * @return a feedback marked as failed
     */
    public static Feedback failure(String message) {
        return new Feedback(message, true);
    }
}