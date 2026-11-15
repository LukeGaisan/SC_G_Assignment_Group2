package com.stoneflamepath.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds every stop and route in the service area.
 */
public final class TransitNetwork {

    private final List<Stop> stops = new ArrayList<>();
    private final List<Route> routes = new ArrayList<>();

    /**
     * Adds a stop to the network.
     *
     * @param stop the stop to add
     */
    public void addStop(Stop stop) {
        stops.add(Objects.requireNonNull(stop, "stop"));
    }

    /**
     * Adds a route to the network. Any of its stops not yet in the network are added too.
     *
     * @param route the route to add
     */
    public void addRoute(Route route) {
        Objects.requireNonNull(route, "route");
        route.getStops().stream().filter(stop -> !stops.contains(stop)).forEach(stops::add);
        routes.add(route);
    }

    /** @return all stops (read-only) */
    public List<Stop> getStops() {
        return List.copyOf(stops);
    }

    /** @return all routes (read-only) */
    public List<Route> getRoutes() {
        return List.copyOf(routes);
    }

    /**
     * Finds the stop closest to a point on the map, e.g. one returned by the geocoder.
     *
     * @param point the location to search from
     * @return the nearest stop, or empty if the network has no stops
     */
    public Optional<Stop> findNearestStop(Coordinate point) {
        return stops.stream()
                .min(Comparator.comparingDouble(stop -> stop.getLocation().distanceTo(point)));
    }

    /**
     * Looks up a route by its identifier.
     *
     * @param routeId the route identifier, e.g. "R1"
     * @return the route, or empty if none has that identifier
     */
    public Optional<Route> findRoute(String routeId) {
        return routes.stream().filter(route -> route.getRouteId().equals(routeId)).findFirst();
    }
}
