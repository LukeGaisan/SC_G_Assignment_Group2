package com.stoneflamepath.planning;

import com.stoneflamepath.model.Journey;

import java.util.Comparator;
import java.util.List;

/**
 * Keeps only journeys with no steps to climb, then ranks them by travel time.
 * Intended for older passengers, wheelchair users and parents with prams.
 */
public class StepFreeStrategy implements JourneyRankingStrategy {

    @Override
    public List<Journey> rank(List<Journey> options) {
        return options.stream()
                .filter(Journey::isStepFree)
                .sorted(Comparator.comparingInt(Journey::getTotalMinutes))
                .toList();
    }

    @Override
    public String getName() {
        return "Step-free";
    }
}
