package com.stoneflamepath.planning;

import com.stoneflamepath.model.Journey;

import java.util.List;

/**
 * A rule for ordering candidate journeys (Strategy pattern).
 * Each implementation is one way a passenger can choose a trip.
 */
public interface JourneyRankingStrategy {

    /**
     * Orders the candidate journeys, best first. A strategy may also drop
     * journeys that do not meet its rule.
     *
     * @param options candidate journeys between the same two stops
     * @return a new list of journeys, best first
     */
    List<Journey> rank(List<Journey> options);

    /** @return the name shown to the user, e.g. "Fastest" */
    String getName();
}
