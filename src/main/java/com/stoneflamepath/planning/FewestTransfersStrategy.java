package com.stoneflamepath.planning;

import com.stoneflamepath.model.Journey;

import java.util.Comparator;
import java.util.List;

/**
 * Ranks journeys by the number of route changes, then by travel time.
 */
public class FewestTransfersStrategy implements JourneyRankingStrategy {

    @Override
    public List<Journey> rank(List<Journey> options) {
        return options.stream()
                .sorted(Comparator.comparingInt(Journey::getTransferCount)
                        .thenComparingInt(Journey::getTotalMinutes))
                .toList();
    }

    @Override
    public String getName() {
        return "Fewest transfers";
    }
}
