package com.stoneflamepath.planning;

import com.stoneflamepath.TestNetwork;
import com.stoneflamepath.model.Journey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static com.stoneflamepath.TestNetwork.*;
import static org.junit.jupiter.api.Assertions.*;

class JourneyPlannerTest {

    private JourneyPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new JourneyPlanner(TestNetwork.build(), new FastestStrategy());
    }

    @Test
    void buildCandidates_ss2ToOldTown_findsTwoDirectAndOneTransfer() {
        List<Journey> candidates = planner.buildCandidates(SS2_HUB, OLD_TOWN);

        assertEquals(3, candidates.size());
        assertEquals(1, candidates.stream().filter(j -> j.getTransferCount() == 1).count());
    }

    @Test
    void plan_fastestStrategy_returnsTransferJourneyFirst() {
        Journey best = planner.plan(SS2_HUB, OLD_TOWN).get(0);

        assertEquals(7, best.getTotalMinutes());
        assertEquals(1, best.getTransferCount());
    }

    @Test
    void plan_cheapestStrategy_returnsBusOneFirst() {
        planner.setStrategy(new CheapestStrategy());

        Journey best = planner.plan(SS2_HUB, OLD_TOWN).get(0);

        assertEquals(new BigDecimal("1.00"), best.getTotalFare());
        assertEquals(BUS_1, best.getLegs().get(0).getRoute());
    }

    @Test
    void plan_stepFreeStrategy_excludesInaccessibleJourneys() {
        planner.setStrategy(new StepFreeStrategy());

        List<Journey> journeys = planner.plan(SS2_HUB, OLD_TOWN);

        assertTrue(journeys.stream().allMatch(Journey::isStepFree));
        assertEquals(LRT, journeys.get(0).getLegs().get(0).getRoute());
    }

    @Test
    void plan_wrongDirection_returnsEmptyList() {
        assertTrue(planner.plan(OLD_TOWN, SS2_HUB).isEmpty());
    }

    @Test
    void plan_nullOrigin_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> planner.plan(null, OLD_TOWN));
    }

    @Test
    void setStrategy_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> planner.setStrategy(null));
    }

    static Stream<Arguments> strategiesAndNames() {
        return Stream.of(
                Arguments.of(new FastestStrategy(), "Fastest"),
                Arguments.of(new CheapestStrategy(), "Cheapest"),
                Arguments.of(new StepFreeStrategy(), "Step-free"));
    }

    @ParameterizedTest
    @MethodSource("strategiesAndNames")
    void getName_eachStrategy_returnsDisplayName(JourneyRankingStrategy strategy, String expected) {
        assertEquals(expected, strategy.getName());
    }
}
