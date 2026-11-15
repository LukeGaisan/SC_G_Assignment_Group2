package com.stoneflamepath.planning;

import com.stoneflamepath.model.Journey;

import java.util.Comparator;
import java.util.List;

/**
 * Ranks journeys by total fare, cheapest first. Equal fares are ordered by travel time.
 */
public class CheapestStrategy implements JourneyRankingStrategy {

    @Override
    public List<Journey> rank(List<Journey> options) {
        return options.stream()
                .sorted(Comparator.comparing(Journey::getTotalFare)
                        .thenComparingInt(Journey::getTotalMinutes))
                .toList();
    }

    @Override
    public String getName() {
        return "Cheapest";
    }
}
