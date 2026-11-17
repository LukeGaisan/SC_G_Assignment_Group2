package org.main;

import com.stoneflamepath.alerts.DisruptionService;
import com.stoneflamepath.alerts.OperationsDashboard;
import com.stoneflamepath.api.NominatimClient;
import com.stoneflamepath.data.SampleNetwork;
import com.stoneflamepath.gui.PlannerController;
import com.stoneflamepath.gui.PlannerFrame;
import com.stoneflamepath.model.TransitNetwork;
import com.stoneflamepath.planning.CheapestStrategy;
import com.stoneflamepath.planning.FastestStrategy;
import com.stoneflamepath.planning.FewestTransfersStrategy;
import com.stoneflamepath.planning.JourneyPlanner;
import com.stoneflamepath.planning.JourneyRankingStrategy;
import com.stoneflamepath.planning.StepFreeStrategy;

import javax.swing.SwingUtilities;
import java.util.List;

/**
 * Starts StoneFlamePath: builds the demo network, wires the layers together
 * and opens the window.
 */
public class Main {

    /**
     * Program entry point.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        TransitNetwork network = SampleNetwork.build();
        List<JourneyRankingStrategy> strategies = List.of(new FastestStrategy(),
                new CheapestStrategy(), new FewestTransfersStrategy(), new StepFreeStrategy());
        DisruptionService disruptions = new DisruptionService();
        OperationsDashboard dashboard = new OperationsDashboard();
        network.getRoutes().forEach(route -> disruptions.subscribe(route.getRouteId(), dashboard));
        PlannerController controller = new PlannerController(network,
                new JourneyPlanner(network, strategies.get(0)), new NominatimClient(), disruptions);
        SwingUtilities.invokeLater(() -> new PlannerFrame(controller, strategies).setVisible(true));
    }
}
