package com.stoneflamepath.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One part of a journey: riding a single route from one stop to another.
 */
public final class Leg {

    private final Stop from;
    private final Stop to;
    private final Route route;

    /**
     * Creates a leg.
     *
     * @param from  boarding stop
     * @param to    alighting stop
     * @param route the route ridden between them
     * @throws IllegalArgumentException if the route does not travel from {@code from} to {@code to}
     */
    public Leg(Stop from, Stop to, Route route) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.route = Objects.requireNonNull(route, "route");
        if (!route.travelsBetween(from, to)) {
            throw new IllegalArgumentException(route + " does not travel from " + from + " to " + to);
        }
    }

    /** @return the boarding stop */
    public Stop getFrom() {
        return from;
    }

    /** @return the alighting stop */
    public Stop getTo() {
        return to;
    }

    /** @return the route ridden on this leg */
    public Route getRoute() {
        return route;
    }

    /** @return travel time for this leg in minutes */
    public int getMinutes() {
        return route.hopsBetween(from, to) * route.getMinutesBetweenStops();
    }

    /** @return the fare for this leg */
    public BigDecimal getFare() {
        return route.getFare();
    }

    /**
     * A leg is step-free only when the boarding stop, the alighting stop
     * and the vehicle are all step-free.
     *
     * @return true if the leg needs no steps
     */
    public boolean isStepFree() {
        return from.isStepFree() && to.isStepFree() && route.isStepFreeVehicle();
    }

    @Override
    public String toString() {
        return route + ": " + from + " -> " + to;
    }
}
