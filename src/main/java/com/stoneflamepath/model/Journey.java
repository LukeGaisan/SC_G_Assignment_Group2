package com.stoneflamepath.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * A complete trip from an origin stop to a destination stop, made of one or more legs.
 */
public final class Journey {

    private final List<Leg> legs;

    /**
     * Creates a journey.
     *
     * @param legs the legs in travel order; must not be empty
     * @throws IllegalArgumentException if {@code legs} is empty
     */
    public Journey(List<Leg> legs) {
        if (legs.isEmpty()) {
            throw new IllegalArgumentException("A journey needs at least one leg");
        }
        this.legs = List.copyOf(legs);
    }

    /** @return the legs in travel order (read-only) */
    public List<Leg> getLegs() {
        return legs;
    }

    /** @return the stop where the journey starts */
    public Stop getOrigin() {
        return legs.get(0).getFrom();
    }

    /** @return the stop where the journey ends */
    public Stop getDestination() {
        return legs.get(legs.size() - 1).getTo();
    }

    /** @return total travel time across all legs, in minutes */
    public int getTotalMinutes() {
        return legs.stream().mapToInt(Leg::getMinutes).sum();
    }

    /** @return total fare across all legs */
    public BigDecimal getTotalFare() {
        return legs.stream().map(Leg::getFare).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** @return number of times the passenger changes route */
    public int getTransferCount() {
        return legs.size() - 1;
    }

    /** @return true if every leg is step-free */
    public boolean isStepFree() {
        return legs.stream().allMatch(Leg::isStepFree);
    }

    @Override
    public String toString() {
        return legs + " (" + getTotalMinutes() + " min, RM" + getTotalFare() + ")";
    }
}
