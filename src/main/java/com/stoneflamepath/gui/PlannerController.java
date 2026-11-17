package com.stoneflamepath.gui;

import com.stoneflamepath.alerts.DelayObserver;
import com.stoneflamepath.alerts.DisruptionService;
import com.stoneflamepath.api.GeocodingException;
import com.stoneflamepath.api.GeocodingService;
import com.stoneflamepath.model.Coordinate;
import com.stoneflamepath.model.Journey;
import com.stoneflamepath.model.Route;
import com.stoneflamepath.model.Stop;
import com.stoneflamepath.model.TransitNetwork;
import com.stoneflamepath.planning.JourneyPlanner;
import com.stoneflamepath.planning.JourneyRankingStrategy;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * The Controller in MVC. It validates what the user typed, finds the places,
 * asks the planner for journeys and hands plain results back to the views.
 * It contains no Swing code, so it can be unit tested.
 */
public class PlannerController {

    private static final double MAX_WALK_KM = 2.0;
    private static final int MIN_DELAY_MINUTES = 1;
    private static final String NEED_BOTH_MESSAGE = "Please enter both a start and a destination.";
    private static final String SAME_PLACE_MESSAGE = "Start and destination must be different places.";
    private static final String OFFLINE_NOTE =
            "The map service is offline, so the last known location was used.";

    private final TransitNetwork network;
    private final JourneyPlanner planner;
    private final GeocodingService geocoder;
    private final DisruptionService disruptions;

    private record Located(Stop stop, boolean fromCache) { }

    /**
     * Creates the controller.
     *
     * @param network     the stops and routes
     * @param planner     plans journeys with the chosen ranking rule
     * @param geocoder    turns place names into map points
     * @param disruptions records and broadcasts delays
     */
    public PlannerController(TransitNetwork network, JourneyPlanner planner,
                             GeocodingService geocoder, DisruptionService disruptions) {
        this.network = Objects.requireNonNull(network, "network");
        this.planner = Objects.requireNonNull(planner, "planner");
        this.geocoder = Objects.requireNonNull(geocoder, "geocoder");
        this.disruptions = Objects.requireNonNull(disruptions, "disruptions");
    }

    /** @return every route, for the route drop-down lists */
    public List<Route> getRoutes() {
        return network.getRoutes();
    }

    /**
     * Plans journeys between two places typed by the user.
     *
     * @param fromText the start, a stop name or a place name
     * @param toText   the destination, a stop name or a place name
     * @param strategy the ranking rule chosen in the window
     * @return ranked journeys, or a friendly explanation of what went wrong
     * @throws NullPointerException if {@code strategy} is null
     */
    public PlanResult planJourney(String fromText, String toText, JourneyRankingStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy");
        Optional<String> problem = validateInputs(fromText, toText);
        if (problem.isPresent()) {
            return PlanResult.failure(problem.get());
        }
        try {
            return planBetween(fromText.trim(), toText.trim(), strategy);
        } catch (GeocodingException serviceDown) {
            return PlanResult.failure(serviceDown.getMessage());
        }
    }

    /**
     * Records a delay on a route after checking what the staff member entered.
     *
     * @param route       the delayed route
     * @param minutesText the delay as typed, in whole minutes
     * @param reason      why the route is delayed
     * @return a message for the status line
     */
    public Feedback logDelay(Route route, String minutesText, String reason) {
        if (route == null) {
            return Feedback.failure("Please choose a route.");
        }
        if (reason == null || reason.isBlank()) {
            return Feedback.failure("Please give a reason for the delay.");
        }
        OptionalInt minutes = parseMinutes(minutesText);
        if (minutes.isEmpty()) {
            return Feedback.failure("Delay must be a whole number of minutes, at least "
                    + MIN_DELAY_MINUTES + ".");
        }
        int toNotify = disruptions.subscriberCount(route.getRouteId());
        disruptions.recordDelay(route, minutes.getAsInt(), reason.trim());
        return Feedback.success("Delay logged. " + toNotify + " subscriber(s) notified.");
    }

    /**
     * Signs an observer up for delay alerts on one route.
     *
     * @param route    the route to follow
     * @param observer who should be told about delays
     * @return a message for the status line
     */
    public Feedback subscribe(Route route, DelayObserver observer) {
        if (route == null) {
            return Feedback.failure("Please choose a route.");
        }
        disruptions.subscribe(route.getRouteId(), observer);
        return Feedback.success("Subscribed to " + route.getName() + ".");
    }

    private Optional<String> validateInputs(String from, String to) {
        if (isBlank(from) || isBlank(to)) {
            return Optional.of(NEED_BOTH_MESSAGE);
        }
        if (from.trim().equalsIgnoreCase(to.trim())) {
            return Optional.of(SAME_PLACE_MESSAGE);
        }
        return Optional.empty();
    }

    private PlanResult planBetween(String fromText, String toText, JourneyRankingStrategy strategy)
            throws GeocodingException {
        Optional<Located> from = locate(fromText);
        if (from.isEmpty()) {
            return PlanResult.failure(notFoundMessage(fromText));
        }
        Optional<Located> to = locate(toText);
        if (to.isEmpty()) {
            return PlanResult.failure(notFoundMessage(toText));
        }
        if (from.get().stop().equals(to.get().stop())) {
            return PlanResult.failure("Both places are nearest to the same stop ("
                    + from.get().stop().getName() + "). Try two places further apart.");
        }
        planner.setStrategy(strategy);
        List<Journey> journeys = planner.plan(from.get().stop(), to.get().stop());
        return describe(journeys, strategy, from.get().fromCache() || to.get().fromCache());
    }

    private Optional<Located> locate(String text) throws GeocodingException {
        Optional<Stop> named = findStopByName(text);
        if (named.isPresent()) {
            return Optional.of(new Located(named.get(), false));
        }
        Optional<Coordinate> point = geocoder.geocode(text);
        boolean cached = geocoder.isLastResultCached();
        return point.flatMap(this::nearestStopWithinWalk).map(stop -> new Located(stop, cached));
    }

    private Optional<Stop> findStopByName(String text) {
        return network.getStops().stream()
                .filter(stop -> stop.getName().equalsIgnoreCase(text))
                .findFirst();
    }

    private Optional<Stop> nearestStopWithinWalk(Coordinate point) {
        return network.findNearestStop(point)
                .filter(stop -> stop.getLocation().distanceTo(point) <= MAX_WALK_KM);
    }

    private PlanResult describe(List<Journey> journeys, JourneyRankingStrategy strategy, boolean cached) {
        if (journeys.isEmpty()) {
            return PlanResult.failure("No journey found with the \"" + strategy.getName()
                    + "\" option. Try another option.");
        }
        return PlanResult.success(journeys, cached ? OFFLINE_NOTE : "");
    }

    private String notFoundMessage(String text) {
        return "We could not find \"" + text + "\" within " + (int) MAX_WALK_KM
                + " km of a stop. Try a more specific place in Petaling Jaya, or a stop name.";
    }

    private OptionalInt parseMinutes(String text) {
        if (text == null) {
            return OptionalInt.empty();
        }
        try {
            int minutes = Integer.parseInt(text.trim());
            return minutes >= MIN_DELAY_MINUTES ? OptionalInt.of(minutes) : OptionalInt.empty();
        } catch (NumberFormatException notANumber) {
            return OptionalInt.empty();
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
