package com.stoneflamepath.gui;

import com.stoneflamepath.model.Route;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

/**
 * View: the control room screen where staff log a delay on a route.
 */
public class StaffPanel extends JPanel {

    private static final int GAP = 8;
    private static final int FIELD_COLUMNS = 20;

    private final PlannerController controller;
    private final JComboBox<Route> routeBox;
    private final JTextField minutesField = new JTextField(FIELD_COLUMNS);
    private final JTextField reasonField = new JTextField(FIELD_COLUMNS);
    private final JButton logButton = new JButton("Log delay");
    private final JLabel statusLabel = new JLabel(" ");

    /**
     * Creates the screen.
     *
     * @param controller the controller that records delays
     */
    public StaffPanel(PlannerController controller) {
        super(new BorderLayout(GAP, GAP));
        this.controller = controller;
        routeBox = new JComboBox<>(controller.getRoutes().toArray(new Route[0]));
        setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        add(buildForm(), BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);
        logButton.addActionListener(event -> logDelay());
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridLayout(0, 2, GAP, GAP));
        form.add(new JLabel("Route:"));
        form.add(routeBox);
        form.add(new JLabel("Delay (minutes):"));
        form.add(minutesField);
        form.add(new JLabel("Reason:"));
        form.add(reasonField);
        form.add(new JLabel());
        form.add(logButton);
        return form;
    }

    private void logDelay() {
        Feedback feedback = controller.logDelay(
                (Route) routeBox.getSelectedItem(), minutesField.getText(), reasonField.getText());
        statusLabel.setForeground(feedback.failed() ? Color.RED.darker() : Color.GREEN.darker());
        statusLabel.setText(feedback.message());
        if (!feedback.failed()) {
            minutesField.setText("");
            reasonField.setText("");
        }
    }
}
