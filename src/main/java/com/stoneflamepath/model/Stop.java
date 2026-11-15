package com.stoneflamepath.model;

import java.util.Objects;

/**
 * A bus or train stop in the network.
 */
public final class Stop {

    private final String stopId;
    private final String name;
    private final Coordinate location;
    private final boolean stepFree;

    /**
     * Creates a stop.
     *
     * @param stopId   unique identifier, e.g. "S1"
     * @param name     display name, e.g. "SS2 Bus Hub"
     * @param location where the stop is on the map
     * @param stepFree true if the stop can be reached without climbing steps
     */
    public Stop(String stopId, String name, Coordinate location, boolean stepFree) {
        this.stopId = Objects.requireNonNull(stopId, "stopId");
        this.name = Objects.requireNonNull(name, "name");
        this.location = Objects.requireNonNull(location, "location");
        this.stepFree = stepFree;
    }

    /** @return the unique stop identifier */
    public String getStopId() {
        return stopId;
    }

    /** @return the display name */
    public String getName() {
        return name;
    }

    /** @return the stop's position on the map */
    public Coordinate getLocation() {
        return location;
    }

    /** @return true if the stop has step-free access */
    public boolean isStepFree() {
        return stepFree;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Stop stop && stopId.equals(stop.stopId);
    }

    @Override
    public int hashCode() {
        return stopId.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
