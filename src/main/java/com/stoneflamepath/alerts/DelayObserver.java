package com.stoneflamepath.alerts;

import com.stoneflamepath.model.Disruption;

/**
 * Anything that wants to hear about delays on a route (Observer pattern).
 */
public interface DelayObserver {

    /**
     * Called once for each delay logged on a route this observer subscribed to.
     *
     * @param disruption the delay that was logged
     */
    void onDelay(Disruption disruption);
}
