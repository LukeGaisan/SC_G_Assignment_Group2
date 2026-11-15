package com.stoneflamepath.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A delay on a route, recorded by control room staff.
 */
public final class Disruption {

    private final Route route;
    private final int delayMinutes;
    private final String reason;
    private final LocalDateTime reportedAt;

    /**
     * Creates a disruption.
     *
     * @param route        the delayed route
     * @param delayMinutes how late services are running; zero or more
     * @param reason       short explanation, e.g. "Road works on Jalan SS2/24"
     * @param reportedAt   when the delay was logged
     * @throws IllegalArgumentException if {@code delayMinutes} is negative
     */
    public Disruption(Route route, int delayMinutes, String reason, LocalDateTime reportedAt) {
        if (delayMinutes < 0) {
            throw new IllegalArgumentException("Delay minutes cannot be negative: " + delayMinutes);
        }
        this.route = Objects.requireNonNull(route, "route");
        this.delayMinutes = delayMinutes;
        this.reason = Objects.requireNonNull(reason, "reason");
        this.reportedAt = Objects.requireNonNull(reportedAt, "reportedAt");
    }

    /** @return the delayed route */
    public Route getRoute() {
        return route;
    }

    /** @return the delay in minutes */
    public int getDelayMinutes() {
        return delayMinutes;
    }

    /** @return the reason for the delay */
    public String getReason() {
        return reason;
    }

    /** @return when the delay was logged */
    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    @Override
    public String toString() {
        return route + " delayed " + delayMinutes + " min: " + reason;
    }
}
