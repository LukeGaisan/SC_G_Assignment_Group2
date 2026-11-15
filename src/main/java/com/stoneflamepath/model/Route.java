package com.stoneflamepath.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * A bus or train line that visits its stops in a fixed order.
 */
public final class Route {

    private static final int MIN_STOPS = 2;

    private final String routeId;
    private final String name;
    private final List<Stop> stops;
    private final int minutesBetweenStops;
    private final BigDecimal fare;
    private final boolean stepFreeVehicle;

    /**
     * Creates a route.
     *
     * @param routeId             unique identifier, e.g. "R1"
     * @param name                display name, e.g. "PJ City Loop"
     * @param stops               stops in travel order; at least two
     * @param minutesBetweenStops travel time between two neighbouring stops
     * @param fare                flat fare charged when boarding this route
     * @param stepFreeVehicle     true if the vehicles have a ramp or low floor
     * @throws IllegalArgumentException if fewer than two stops or a non-positive time is given
     */
    public Route(String routeId, String name, List<Stop> stops,
                 int minutesBetweenStops, BigDecimal fare, boolean stepFreeVehicle) {
        if (stops.size() < MIN_STOPS) {
            throw new IllegalArgumentException("A route needs at least " + MIN_STOPS + " stops");
        }
        if (minutesBetweenStops <= 0) {
            throw new IllegalArgumentException("minutesBetweenStops must be positive");
        }
        this.routeId = Objects.requireNonNull(routeId, "routeId");
        this.name = Objects.requireNonNull(name, "name");
        this.stops = List.copyOf(stops);
        this.minutesBetweenStops = minutesBetweenStops;
        this.fare = Objects.requireNonNull(fare, "fare");
        this.stepFreeVehicle = stepFreeVehicle;
    }

    /** @return the unique route identifier */
    public String getRouteId() {
        return routeId;
    }

    /** @return the display name */
    public String getName() {
        return name;
    }

    /** @return the stops in travel order (read-only) */
    public List<Stop> getStops() {
        return stops;
    }

    /** @return travel time in minutes between two neighbouring stops */
    public int getMinutesBetweenStops() {
        return minutesBetweenStops;
    }

    /** @return the flat fare for boarding this route */
    public BigDecimal getFare() {
        return fare;
    }

    /** @return true if the vehicles on this route are step-free */
    public boolean isStepFreeVehicle() {
        return stepFreeVehicle;
    }

    /**
     * Checks whether this route can carry a passenger from one stop to another,
     * which requires both stops on the route with {@code from} before {@code to}.
     *
     * @param from boarding stop
     * @param to   alighting stop
     * @return true if the route travels from {@code from} to {@code to}
     */
    public boolean travelsBetween(Stop from, Stop to) {
        int fromIndex = stops.indexOf(from);
        int toIndex = stops.indexOf(to);
        return fromIndex >= 0 && toIndex > fromIndex;
    }

    /**
     * Counts how many stops apart two stops are on this route.
     *
     * @param from boarding stop
     * @param to   alighting stop
     * @return number of hops from {@code from} to {@code to}
     * @throws IllegalArgumentException if the route does not travel between them
     */
    public int hopsBetween(Stop from, Stop to) {
        if (!travelsBetween(from, to)) {
            throw new IllegalArgumentException(name + " does not travel from " + from + " to " + to);
        }
        return stops.indexOf(to) - stops.indexOf(from);
    }

    @Override
    public String toString() {
        return name;
    }
}
