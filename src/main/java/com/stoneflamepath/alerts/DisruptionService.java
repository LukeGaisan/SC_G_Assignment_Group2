package com.stoneflamepath.alerts;

import com.stoneflamepath.model.Disruption;
import com.stoneflamepath.model.Route;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Records delays and tells every subscriber of the affected route straight away
 * (the subject in the Observer pattern). It does not know who its subscribers are,
 * only that they implement {@link DelayObserver}.
 */
public class DisruptionService {

    private final Map<String, List<DelayObserver>> subscribers = new HashMap<>();
    private final Clock clock;

    /** Creates a service that timestamps delays with the system clock. */
    public DisruptionService() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates a service with a fixed clock, so tests get predictable timestamps.
     *
     * @param clock the clock used to timestamp delays
     */
    public DisruptionService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Signs an observer up for delays on one route.
     *
     * @param routeId  the route to follow, e.g. "R1"
     * @param observer who to notify
     */
    public void subscribe(String routeId, DelayObserver observer) {
        Objects.requireNonNull(observer, "observer");
        subscribers.computeIfAbsent(routeId, id -> new ArrayList<>()).add(observer);
    }

    /**
     * Stops an observer receiving delays for one route.
     *
     * @param routeId  the route to stop following
     * @param observer who to remove
     */
    public void unsubscribe(String routeId, DelayObserver observer) {
        subscribers.getOrDefault(routeId, new ArrayList<>()).remove(observer);
    }

    /**
     * Logs a delay and notifies everyone subscribed to that route.
     *
     * @param route   the delayed route
     * @param minutes how late services are running; zero or more
     * @param reason  short explanation shown to passengers
     * @return the recorded disruption
     * @throws IllegalArgumentException if {@code minutes} is negative
     */
    public Disruption recordDelay(Route route, int minutes, String reason) {
        Disruption disruption = new Disruption(route, minutes, reason, LocalDateTime.now(clock));
        notifySubscribers(disruption);
        return disruption;
    }

    /**
     * @param routeId the route to check
     * @return how many observers follow that route
     */
    public int subscriberCount(String routeId) {
        return subscribers.getOrDefault(routeId, List.of()).size();
    }

    private void notifySubscribers(Disruption disruption) {
        String routeId = disruption.getRoute().getRouteId();
        List.copyOf(subscribers.getOrDefault(routeId, List.of()))
                .forEach(observer -> observer.onDelay(disruption));
    }
}
