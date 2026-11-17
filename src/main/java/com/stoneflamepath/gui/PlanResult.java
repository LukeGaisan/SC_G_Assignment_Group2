package com.stoneflamepath.gui;

import com.stoneflamepath.model.Journey;

import java.util.List;

/**
 * What the controller hands back to the view after a journey search.
 *
 * @param journeys ranked journeys, best first; empty on failure
 * @param message  text for the status line; empty when there is nothing to add
 * @param failed   true if the search could not produce journeys
 */
public record PlanResult(List<Journey> journeys, String message, boolean failed) {

    /**
     * Creates a failed result.
     *
     * @param message a friendly explanation for the user
     * @return a result with no journeys
     */
    public static PlanResult failure(String message) {
        return new PlanResult(List.of(), message, true);
    }

    /**
     * Creates a successful result.
     *
     * @param journeys the ranked journeys
     * @param note     an optional note for the user, or an empty string
     * @return a result carrying the journeys
     */
    public static PlanResult success(List<Journey> journeys, String note) {
        return new PlanResult(journeys, note, false);
    }
}