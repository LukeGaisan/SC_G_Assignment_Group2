package com.stoneflamepath.planning;

import com.stoneflamepath.model.Journey;
import com.stoneflamepath.model.Leg;
import com.stoneflamepath.model.Route;
import com.stoneflamepath.model.Stop;
import com.stoneflamepath.model.TransitNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Finds journeys between two stops and orders them with the current
 * {@link JourneyRankingStrategy}. Supports direct trips and trips with one transfer.
 */
public class JourneyPlanner {

    private final TransitNetwork network;
    private JourneyRankingStrategy strategy;

    /**
     * Creates a planner.
     *
     * @param network  the stops and routes to plan over
     * @param strategy the ranking rule to start with
     */
    public JourneyPlanner(TransitNetwork network, JourneyRankingStrategy strategy) {
        this.network = Objects.requireNonNull(network, "network");
        setStrategy(strategy);
    }

    /**
     * Swaps the ranking rule, e.g. when the user picks "Cheapest" in the window.
     *
     * @param strategy the new ranking rule
     * @throws NullPointerException if {@code strategy} is null
     */
    public void setStrategy(JourneyRankingStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    /** @return the ranking rule currently in use */
    public JourneyRankingStrategy getStrategy() {
        return strategy;
    }

    /**
     * Plans journeys from origin to destination, best first by the current strategy.
     *
     * @param origin      starting stop
     * @param destination finishing stop
     * @return ranked journeys; empty if no journey exists or none meets the strategy's rule
     * @throws NullPointerException if either stop is null
     */
    public List<Journey> plan(Stop origin, Stop destination) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(destination, "destination");
        return strategy.rank(buildCandidates(origin, destination));
    }

    /**
     * Lists every direct journey and every journey with exactly one transfer.
     *
     * @param origin      starting stop
     * @param destination finishing stop
     * @return all candidate journeys, in no particular order
     */
    List<Journey> buildCandidates(Stop origin, Stop destination) {
        List<Journey> candidates = new ArrayList<>(findDirectJourneys(origin, destination));
        candidates.addAll(findOneTransferJourneys(origin, destination));
        return candidates;
    }

    private List<Journey> findDirectJourneys(Stop origin, Stop destination) {
        return network.getRoutes().stream()
                .filter(route -> route.travelsBetween(origin, destination))
                .map(route -> new Journey(List.of(new Leg(origin, destination, route))))
                .toList();
    }

    private List<Journey> findOneTransferJourneys(Stop origin, Stop destination) {
        List<Journey> journeys = new ArrayList<>();
        for (Route firstRoute : network.getRoutes()) {
            for (Stop transfer : firstRoute.getStops()) {
                if (firstRoute.travelsBetween(origin, transfer) && !transfer.equals(destination)) {
                    journeys.addAll(continueFrom(transfer, firstRoute, origin, destination));
                }
            }
        }
        return journeys;
    }

    private List<Journey> continueFrom(Stop transfer, Route firstRoute, Stop origin, Stop destination) {
        Leg firstLeg = new Leg(origin, transfer, firstRoute);
        return network.getRoutes().stream()
                .filter(secondRoute -> !secondRoute.equals(firstRoute))
                .filter(secondRoute -> secondRoute.travelsBetween(transfer, destination))
                .map(secondRoute -> new Journey(List.of(firstLeg, new Leg(transfer, destination, secondRoute))))
                .toList();
    }
}
