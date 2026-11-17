package com.stoneflamepath.gui;

import com.stoneflamepath.alerts.DelayObserver;
import com.stoneflamepath.model.Disruption;
import com.stoneflamepath.model.Route;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * View: the "Delay Alerts" screen. It is also an Observer: once a passenger
 * subscribes to a route, every delay logged on that route appears in the list.
 */
public class AlertsPanel extends JPanel implements DelayObserver {

    private static final int GAP = 8;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final PlannerController controller;
    private final JComboBox<Route> routeBox;
    private final JButton subscribeButton = new JButton("Subscribe");
    private final DefaultListModel<String> alerts = new DefaultListModel<>();
    private final Set<String> subscribedRouteIds = new HashSet<>();
    private final JLabel statusLabel = new JLabel(" ");

    /**
     * Creates the screen.
     *
     * @param controller the controller that signs the passenger up
     */
    public AlertsPanel(PlannerController controller) {
        super(new BorderLayout(GAP, GAP));
        this.controller = controller;
        routeBox = new JComboBox<>(controller.getRoutes().toArray(new Route[0]));
        setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        add(buildTopBar(), BorderLayout.NORTH);
        add(new JScrollPane(new JList<>(alerts)), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        subscribeButton.addActionListener(event -> subscribe());
    }

    @Override
    public void onDelay(Disruption disruption) {
        String line = disruption.getReportedAt().format(TIME_FORMAT) + "  "
                + disruption.getRoute().getName() + " is running "
                + disruption.getDelayMinutes() + " min late: " + disruption.getReason();
        SwingUtilities.invokeLater(() -> alerts.add(0, line));
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        bar.add(new JLabel("Route:"));
        bar.add(routeBox);
        bar.add(subscribeButton);
        return bar;
    }

    private void subscribe() {
        Route route = (Route) routeBox.getSelectedItem();
        if (route != null && !subscribedRouteIds.add(route.getRouteId())) {
            showStatus("You are already subscribed to " + route.getName() + ".", false);
            return;
        }
        Feedback feedback = controller.subscribe(route, this);
        showStatus(feedback.message(), feedback.failed());
    }

    private void showStatus(String message, boolean failed) {
        statusLabel.setForeground(failed ? Color.RED.darker() : Color.GREEN.darker());
        statusLabel.setText(message);
    }
}