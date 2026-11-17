package com.stoneflamepath.gui;

import com.stoneflamepath.TestNetwork;
import com.stoneflamepath.alerts.Commuter;
import com.stoneflamepath.alerts.DisruptionService;
import com.stoneflamepath.api.GeocodingException;
import com.stoneflamepath.api.GeocodingService;
import com.stoneflamepath.model.Coordinate;
import com.stoneflamepath.planning.FastestStrategy;
import com.stoneflamepath.planning.JourneyPlanner;
import com.stoneflamepath.planning.StepFreeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlannerControllerTest {

    private static class StubGeocoder implements GeocodingService {
        private final Map<String, Coordinate> places = new HashMap<>();
        private boolean serviceDown;
        private boolean cached;

        @Override
        public Optional<Coordinate> geocode(String address) throws GeocodingException {
            if (serviceDown) {
                throw new GeocodingException("The map service is not available right now.");
            }
            return Optional.ofNullable(places.get(address.toLowerCase()));
        }

        @Override
        public boolean isLastResultCached() {
            return cached;
        }
    }

    private StubGeocoder geocoder;
    private DisruptionService disruptions;
    private PlannerController controller;

    @BeforeEach
    void setUp() {
        geocoder = new StubGeocoder();
        geocoder.places.put("paradigm area", new Coordinate(3.1051, 101.5961));
        geocoder.places.put("old town area", new Coordinate(3.0881, 101.6461));
        geocoder.places.put("kuala lumpur", new Coordinate(3.1390, 101.6869));
        disruptions = new DisruptionService();
        controller = new PlannerController(TestNetwork.build(),
                new JourneyPlanner(TestNetwork.build(), new FastestStrategy()), geocoder, disruptions);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void planJourney_blankStart_returnsFailureAskingForBoth(String blank) {
        PlanResult result = controller.planJourney(blank, "PJ Old Town", new FastestStrategy());

        assertAll(
                () -> assertTrue(result.failed()),
                () -> assertTrue(result.message().contains("both")));
    }

    @Test
    void planJourney_sameStartAndDestination_returnsFailure() {
        PlanResult result = controller.planJourney("PJ Old Town", " pj old town ", new FastestStrategy());

        assertTrue(result.failed());
    }

    @Test
    void planJourney_nullStrategy_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> controller.planJourney("SS2 Hub", "PJ Old Town", null));
    }

    @Test
    void planJourney_twoStopNames_returnsJourneysWithoutCallingMapService() {
        geocoder.serviceDown = true;

        PlanResult result = controller.planJourney("SS2 Hub", "PJ Old Town", new FastestStrategy());

        assertAll(
                () -> assertFalse(result.failed()),
                () -> assertFalse(result.journeys().isEmpty()));
    }

    @Test
    void planJourney_placeNamesNearStops_findsNearestStopsAndPlans() {
        PlanResult result = controller.planJourney("Paradigm Area", "Old Town Area", new FastestStrategy());

        assertAll(
                () -> assertFalse(result.failed()),
                () -> assertEquals("Paradigm Mall", result.journeys().get(0).getOrigin().getName()),
                () -> assertEquals("PJ Old Town", result.journeys().get(0).getDestination().getName()));
    }

    @Test
    void planJourney_unknownPlace_returnsNotFoundMessage() {
        PlanResult result = controller.planJourney("Atlantis", "PJ Old Town", new FastestStrategy());

        assertAll(
                () -> assertTrue(result.failed()),
                () -> assertTrue(result.message().contains("Atlantis")));
    }

    @Test
    void planJourney_placeFarFromAnyStop_returnsNotFoundMessage() {
        PlanResult result = controller.planJourney("Kuala Lumpur", "PJ Old Town", new FastestStrategy());

        assertTrue(result.failed());
    }

    @Test
    void planJourney_mapServiceDown_returnsFriendlyMessage() {
        geocoder.serviceDown = true;

        PlanResult result = controller.planJourney("Paradigm Area", "PJ Old Town", new FastestStrategy());

        assertAll(
                () -> assertTrue(result.failed()),
                () -> assertTrue(result.message().contains("not available")));
    }

    @Test
    void planJourney_lastKnownLocationUsed_addsOfflineNote() {
        geocoder.cached = true;

        PlanResult result = controller.planJourney("Paradigm Area", "PJ Old Town", new FastestStrategy());

        assertAll(
                () -> assertFalse(result.failed()),
                () -> assertTrue(result.message().contains("offline")));
    }

    @Test
    void planJourney_stepFreeOptionWithNoStepFreeRoute_returnsNoJourneyMessage() {
        PlanResult result = controller.planJourney("Paradigm Mall", "Taman Bahagia", new StepFreeStrategy());

        assertAll(
                () -> assertTrue(result.failed()),
                () -> assertTrue(result.message().contains("Step-free")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "0", "-5", "", "2.5"})
    void logDelay_invalidMinutes_returnsFailure(String minutes) {
        Feedback feedback = controller.logDelay(TestNetwork.BUS_1, minutes, "Breakdown");

        assertTrue(feedback.failed());
    }

    @Test
    void logDelay_blankReason_returnsFailure() {
        assertTrue(controller.logDelay(TestNetwork.BUS_1, "10", "  ").failed());
    }

    @Test
    void logDelay_validDelay_notifiesSubscribersOfThatRouteOnly() {
        Commuter onBus1 = new Commuter("Aina");
        Commuter onLrt = new Commuter("Raj");
        controller.subscribe(TestNetwork.BUS_1, onBus1);
        controller.subscribe(TestNetwork.LRT, onLrt);

        Feedback feedback = controller.logDelay(TestNetwork.BUS_1, "10", "Breakdown");

        assertAll(
                () -> assertFalse(feedback.failed()),
                () -> assertEquals(1, onBus1.getAlerts().size()),
                () -> assertEquals(0, onLrt.getAlerts().size()));
    }
}
