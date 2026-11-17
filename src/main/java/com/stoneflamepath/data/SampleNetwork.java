package com.stoneflamepath.data;

import com.stoneflamepath.model.Coordinate;
import com.stoneflamepath.model.Route;
import com.stoneflamepath.model.Stop;
import com.stoneflamepath.model.TransitNetwork;

import java.math.BigDecimal;
import java.util.List;

/**
 * The demo network for Petaling Jaya: 8 stops and 3 routes.
 */
public final class SampleNetwork {

    private static final Stop SS2_HUB = stop("S1", "SS2 Hub", 3.1180, 101.6220, true);
    private static final Stop PARADIGM = stop("S2", "Paradigm Mall", 3.1050, 101.5960, true);
    private static final Stop TAMAN_BAHAGIA = stop("S3", "Taman Bahagia", 3.1100, 101.6130, false);
    private static final Stop OLD_TOWN = stop("S4", "PJ Old Town", 3.0880, 101.6460, true);
    private static final Stop KELANA_JAYA = stop("S5", "Kelana Jaya", 3.1120, 101.6040, true);
    private static final Stop UNIVERSITI = stop("S6", "Universiti", 3.1140, 101.6610, true);
    private static final Stop SECTION_14 = stop("S7", "Section 14", 3.1010, 101.6370, true);
    private static final Stop TAMAN_JAYA = stop("S8", "Taman Jaya", 3.1040, 101.6450, true);

    private SampleNetwork() {
    }

    /**
     * Builds the demo network.
     *
     * @return a network with 8 stops and 3 routes
     */
    public static TransitNetwork build() {
        TransitNetwork network = new TransitNetwork();
        network.addRoute(new Route("R1", "Bus 1",
                List.of(SS2_HUB, PARADIGM, TAMAN_BAHAGIA, OLD_TOWN), 5, new BigDecimal("1.00"), false));
        network.addRoute(new Route("R2", "LRT",
                List.of(KELANA_JAYA, SS2_HUB, OLD_TOWN, UNIVERSITI), 4, new BigDecimal("2.50"), true));
        network.addRoute(new Route("R3", "Bus 3",
                List.of(TAMAN_BAHAGIA, SECTION_14, TAMAN_JAYA, UNIVERSITI), 3, new BigDecimal("0.50"), true));
        return network;
    }

    private static Stop stop(String id, String name, double latitude, double longitude, boolean stepFree) {
        return new Stop(id, name, new Coordinate(latitude, longitude), stepFree);
    }
}