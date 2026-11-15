package com.stoneflamepath.alerts;

import com.stoneflamepath.model.Disruption;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A passenger who follows routes and receives delay alerts for them.
 */
public class Commuter implements DelayObserver {

    private final String name;
    private final List<String> alerts = new ArrayList<>();

    /**
     * Creates a commuter.
     *
     * @param name the commuter's display name
     */
    public Commuter(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /** @return the commuter's display name */
    public String getName() {
        return name;
    }

    /**
     * Turns the delay into a readable alert message and stores it.
     *
     * @param disruption the delay that was logged
     */
    @Override
    public void onDelay(Disruption disruption) {
        alerts.add(disruption.getRoute().getName() + " is running "
                + disruption.getDelayMinutes() + " min late: " + disruption.getReason());
    }

    /** @return alert messages received so far, oldest first (read-only) */
    public List<String> getAlerts() {
        return List.copyOf(alerts);
    }
}
