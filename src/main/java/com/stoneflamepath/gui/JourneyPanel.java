package com.stoneflamepath.gui;

import com.stoneflamepath.model.Journey;
import com.stoneflamepath.planning.JourneyRankingStrategy;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * View: the "Plan Journey" screen. It shows what the controller returns and
 * never talks to the model or the map service itself.
 */
public class JourneyPanel extends JPanel {

    private static final int GAP = 8;
    private static final int FIELD_COLUMNS = 20;
    private static final Color ERROR_COLOUR = Color.RED.darker();
    private static final Color WARNING_COLOUR = Color.ORANGE.darker();
    private static final Color OK_COLOUR = Color.GREEN.darker();
    private static final String UNEXPECTED_ERROR = "Something went wrong. Please try again.";

    private final PlannerController controller;
    private final Map<String, JourneyRankingStrategy> strategies = new LinkedHashMap<>();
    private final JTextField fromField = new JTextField(FIELD_COLUMNS);
    private final JTextField toField = new JTextField(FIELD_COLUMNS);
    private final JComboBox<String> strategyBox;
    private final JButton planButton = new JButton("Plan journey");
    private final JTextArea resultArea = new JTextArea();
    private final JLabel statusLabel = new JLabel(" ");

    /**
     * Creates the screen.
     *
     * @param controller the controller that does the work
     * @param options    the ranking rules the user can pick from
     */
    public JourneyPanel(PlannerController controller, List<JourneyRankingStrategy> options) {
        super(new BorderLayout(GAP, GAP));
        this.controller = controller;
        options.forEach(option -> strategies.put(option.getName(), option));
        strategyBox = new JComboBox<>(strategies.keySet().toArray(new String[0]));
        resultArea.setEditable(false);
        setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        planButton.addActionListener(event -> startPlanning());
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridLayout(0, 2, GAP, GAP));
        form.add(new JLabel("From:"));
        form.add(fromField);
        form.add(new JLabel("To:"));
        form.add(toField);
        form.add(new JLabel("Option:"));
        form.add(strategyBox);
        form.add(new JLabel());
        form.add(planButton);
        return form;
    }

    private void startPlanning() {
        String from = fromField.getText();
        String to = toField.getText();
        JourneyRankingStrategy strategy = strategies.get((String) strategyBox.getSelectedItem());
        planButton.setEnabled(false);
        showStatus("Searching...", Color.DARK_GRAY);
        new SwingWorker<PlanResult, Void>() {
            @Override
            protected PlanResult doInBackground() {
                return controller.planJourney(from, to, strategy);
            }

            @Override
            protected void done() {
                finishPlanning(this);
            }
        }.execute();
    }

    private void finishPlanning(SwingWorker<PlanResult, Void> worker) {
        planButton.setEnabled(true);
        try {
            display(worker.get());
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            showStatus(UNEXPECTED_ERROR, ERROR_COLOUR);
        } catch (ExecutionException failure) {
            showStatus(UNEXPECTED_ERROR, ERROR_COLOUR);
        }
    }

    private void display(PlanResult result) {
        resultArea.setText(formatJourneys(result.journeys()));
        if (result.failed()) {
            showStatus(result.message(), ERROR_COLOUR);
        } else if (!result.message().isEmpty()) {
            showStatus(result.message(), WARNING_COLOUR);
        } else {
            showStatus("Showing " + result.journeys().size() + " journey(s), best first.", OK_COLOUR);
        }
    }

    private String formatJourneys(List<Journey> journeys) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < journeys.size(); i++) {
            text.append(i + 1).append(". ").append(journeys.get(i)).append(System.lineSeparator());
        }
        return text.toString();
    }

    private void showStatus(String message, Color colour) {
        statusLabel.setForeground(colour);
        statusLabel.setText(message);
    }
}
