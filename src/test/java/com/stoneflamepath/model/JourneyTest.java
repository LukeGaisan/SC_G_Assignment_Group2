package com.stoneflamepath.model;

import com.stoneflamepath.TestNetwork;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.stoneflamepath.TestNetwork.*;
import static org.junit.jupiter.api.Assertions.*;

class JourneyTest {

    @Test
    void constructor_emptyLegList_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Journey(List.of()));
    }

    @Test
    void getters_transferJourney_reportAllTotals() {
        Journey journey = new Journey(List.of(
                new Leg(SS2_HUB, TAMAN_BAHAGIA, BUS_3),
                new Leg(TAMAN_BAHAGIA, OLD_TOWN, BUS_1)));

        assertAll(
                () -> assertEquals(SS2_HUB, journey.getOrigin()),
                () -> assertEquals(OLD_TOWN, journey.getDestination()),
                () -> assertEquals(7, journey.getTotalMinutes()),
                () -> assertEquals(new BigDecimal("1.50"), journey.getTotalFare()),
                () -> assertEquals(1, journey.getTransferCount()),
                () -> assertFalse(journey.isStepFree()));
    }

    @Test
    void leg_routeDoesNotServeStops_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Leg(SS2_HUB, KELANA_JAYA, BUS_1));
    }

    @Test
    void findNearestStop_pointNextToParadigm_returnsParadigm() {
        TransitNetwork network = TestNetwork.build();

        Stop nearest = network.findNearestStop(new Coordinate(3.1051, 101.5961)).orElseThrow();

        assertEquals(PARADIGM, nearest);
    }
}
