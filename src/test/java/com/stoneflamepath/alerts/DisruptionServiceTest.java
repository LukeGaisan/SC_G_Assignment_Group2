package com.stoneflamepath.alerts;

import com.stoneflamepath.model.Disruption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.stoneflamepath.TestNetwork.*;
import static org.junit.jupiter.api.Assertions.*;

class DisruptionServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T08:00:00Z");

    private DisruptionService service;
    private Commuter commuter;
    private OperationsDashboard dashboard;

    @BeforeEach
    void setUp() {
        service = new DisruptionService(Clock.fixed(NOW, ZoneOffset.UTC));
        commuter = new Commuter("Aisyah");
        dashboard = new OperationsDashboard();
        service.subscribe(BUS_1.getRouteId(), commuter);
        service.subscribe(BUS_1.getRouteId(), dashboard);
    }

    @Test
    void recordDelay_subscribedRoute_notifiesAllObservers() {
        service.recordDelay(BUS_1, 15, "Road works");

        assertEquals(1, commuter.getAlerts().size());
        assertEquals(1, dashboard.getDelayLog().size());
    }

    @Test
    void recordDelay_otherRoute_doesNotNotify() {
        service.recordDelay(LRT, 10, "Signal fault");

        assertTrue(commuter.getAlerts().isEmpty());
        assertTrue(dashboard.getDelayLog().isEmpty());
    }

    @Test
    void recordDelay_afterUnsubscribe_doesNotNotifyCommuter() {
        service.unsubscribe(BUS_1.getRouteId(), commuter);

        service.recordDelay(BUS_1, 5, "Heavy traffic");

        assertTrue(commuter.getAlerts().isEmpty());
        assertEquals(1, dashboard.getDelayLog().size());
    }

    @Test
    void recordDelay_negativeMinutes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> service.recordDelay(BUS_1, -5, "Typo"));
    }

    @Test
    void recordDelay_validDelay_buildsDisruptionWithAllFields() {
        Disruption disruption = service.recordDelay(BUS_1, 20, "Accident");

        assertAll(
                () -> assertEquals(BUS_1, disruption.getRoute()),
                () -> assertEquals(20, disruption.getDelayMinutes()),
                () -> assertEquals("Accident", disruption.getReason()),
                () -> assertEquals(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC), disruption.getReportedAt()),
                () -> assertEquals("Bus 1 is running 20 min late: Accident", commuter.getAlerts().get(0)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 30, 120})
    void recordDelay_validMinutes_addsMinutesToDashboardTotal(int minutes) {
        service.recordDelay(BUS_1, minutes, "Test");

        assertEquals(minutes, dashboard.getTotalDelayMinutes());
    }
}
