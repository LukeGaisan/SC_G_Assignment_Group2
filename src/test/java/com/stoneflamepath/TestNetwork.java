package com.stoneflamepath;

import com.stoneflamepath.model.Coordinate;
import com.stoneflamepath.model.Route;
import com.stoneflamepath.model.Stop;
import com.stoneflamepath.model.TransitNetwork;

import java.math.BigDecimal;
import java.util.List;

/**
 * A small Petaling Jaya network shared by the tests.
 *
 * <pre>
 * Bus 1 (R1): SS2 Hub -> Paradigm Mall -> Taman Bahagia -> PJ Old Town   5 min/hop, RM1.00
 * LRT   (R2): SS2 Hub -> Kelana Jaya   -> PJ Old Town                    4 min/hop, RM2.50
 * Bus 3 (R3): SS2 Hub -> Taman Bahagia                                   2 min/hop, RM0.50
 * </pre>
 * Taman Bahagia has stairs only, so any journey that boards or alights there is not step-free.
 * From SS2 Hub to PJ Old Town: fastest = Bus 3 + Bus 1 (7 min), cheapest = Bus 1 (RM1.00),
 * step-free = LRT (8 min).
 */
public final class TestNetwork {

    public static final Stop SS2_HUB = new Stop("S1", "SS2 Hub", new Coordinate(3.1180, 101.6220), true);
    public static final Stop PARADIGM = new Stop("S2", "Paradigm Mall", new Coordinate(3.1050, 101.5960), true);
    public static final Stop TAMAN_BAHAGIA = new Stop("S3", "Taman Bahagia", new Coordinate(3.1100, 101.6130), false);
    public static final Stop OLD_TOWN = new Stop("S4", "PJ Old Town", new Coordinate(3.0880, 101.6460), true);
    public static final Stop KELANA_JAYA = new Stop("S5", "Kelana Jaya", new Coordinate(3.1120, 101.6040), true);

    public static final Route BUS_1 = new Route("R1", "Bus 1",
            List.of(SS2_HUB, PARADIGM, TAMAN_BAHAGIA, OLD_TOWN), 5, new BigDecimal("1.00"), true);
    public static final Route LRT = new Route("R2", "LRT",
            List.of(SS2_HUB, KELANA_JAYA, OLD_TOWN), 4, new BigDecimal("2.50"), true);
    public static final Route BUS_3 = new Route("R3", "Bus 3",
            List.of(SS2_HUB, TAMAN_BAHAGIA), 2, new BigDecimal("0.50"), true);

    private TestNetwork() {
    }

    /** @return a fresh network containing the three routes above */
    public static TransitNetwork build() {
        TransitNetwork network = new TransitNetwork();
        network.addRoute(BUS_1);
        network.addRoute(LRT);
        network.addRoute(BUS_3);
        return network;
    }
}
