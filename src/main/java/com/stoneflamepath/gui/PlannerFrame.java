package com.stoneflamepath.gui;

import com.stoneflamepath.planning.JourneyRankingStrategy;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.util.List;

/**
 * The main window. It holds the three screens as tabs.
 */
public class PlannerFrame extends JFrame {

    private static final int WIDTH = 720;
    private static final int HEIGHT = 480;

    /**
     * Creates the window.
     *
     * @param controller the controller shared by all screens
     * @param strategies the ranking rules offered on the journey screen
     */
    public PlannerFrame(PlannerController controller, List<JourneyRankingStrategy> strategies) {
        super("StoneFlamePath - Petaling Jaya Journey Planner");
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Plan Journey", new JourneyPanel(controller, strategies));
        tabs.addTab("Delay Alerts", new AlertsPanel(controller));
        tabs.addTab("Staff Panel", new StaffPanel(controller));
        add(tabs);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}
