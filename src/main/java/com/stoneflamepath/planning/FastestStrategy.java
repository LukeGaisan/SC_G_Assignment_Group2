package com.stoneflamepath.planning;

import com.stoneflamepath.model.Journey;

import java.util.Comparator;
import java.util.List;

/**
 * Ranks journeys by total travel time, shortest first.
 */
public class FastestStrategy implements JourneyRankingStrategy {

    @Override
    public List<Journey> rank(List<Journey> options) {
        return options.stream()
                .sorted(Comparator.comparingInt(Journey::getTotalMinutes))
                .toList();
    }

    @Override
    public String getName() {
        return "Fastest";
    }
}
