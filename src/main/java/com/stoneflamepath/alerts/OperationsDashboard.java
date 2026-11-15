package com.stoneflamepath.alerts;

import com.stoneflamepath.model.Disruption;

import java.util.ArrayList;
import java.util.List;

/**
 * The control room / transport planner view: keeps a log of every delay it is told about.
 */
public class OperationsDashboard implements DelayObserver {

    private final List<Disruption> delayLog = new ArrayList<>();

    /**
     * Adds the delay to the log.
     *
     * @param disruption the delay that was logged
     */
    @Override
    public void onDelay(Disruption disruption) {
        delayLog.add(disruption);
    }

    /** @return every delay received so far, oldest first (read-only) */
    public List<Disruption> getDelayLog() {
        return List.copyOf(delayLog);
    }

    /** @return the sum of delay minutes across the log */
    public int getTotalDelayMinutes() {
        return delayLog.stream().mapToInt(Disruption::getDelayMinutes).sum();
    }
}
