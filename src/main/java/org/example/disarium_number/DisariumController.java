package org.example.disarium_number;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class DisariumController {
    @FXML private TextField upperLimitField;
    @FXML private TextField threadCountField;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;
    @FXML private FlowPane workerPane;
    @FXML private TextArea resultsArea;
    @FXML private Label disariumCountLabel;
    @FXML private Label nonDisariumCountLabel;
    @FXML private Label totalCheckedLabel;

    private final List<SearchTask> workers = new ArrayList<>();
    private int finishedWorkers;

    @FXML
    private void initialize() {
        setRunning(false);
        clearResults();
        errorLabel.managedProperty().bind(errorLabel.visibleProperty());
        errorLabel.setVisible(false);
    }

    @FXML
    private void startSearch() {
        if (!workers.isEmpty()) {
            return;
        }

        int[] inputs;
        try {
            inputs = DisariumSearch.parseInputs(upperLimitField.getText(), threadCountField.getText());
        } catch (IllegalArgumentException exception) {
            errorLabel.setText(exception.getMessage());
            errorLabel.setVisible(true);
            return;
        }

        int limit = inputs[0];
        List<DisariumSearch.Range> ranges = DisariumSearch.ranges(limit, inputs[1]);
        errorLabel.setVisible(false);
        clearResults();
        workerPane.getChildren().clear();
        finishedWorkers = 0;
        setRunning(true);
        for (DisariumSearch.Range range : ranges) {
            SearchTask worker = new SearchTask(range);
            workers.add(worker);
            addWorkerView(worker, workers.size(), limit);
        }
        showRunningStatus();
        for (int i = 0; i < workers.size(); i++) {
            Thread thread = new Thread(workers.get(i), "disarium-worker-" + (i + 1));
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void addWorkerView(SearchTask worker, int number, int limit) {
        DisariumSearch.Range range = worker.range;
        Label title = new Label("Thread " + number);
        ProgressIndicator progress = new ProgressIndicator(0);
        progress.setPrefSize(48, 48);
        progress.progressProperty().bind(worker.progressProperty());
        Label bounds = new Label(range.start() + " to " + range.end());
        bounds.setWrapText(true);
        bounds.setMaxWidth(Double.MAX_VALUE);
        Label count = new Label(range.size() + (range.size() == 1 ? " number" : " numbers"));
        Label state = new Label("Ready");

        VBox tile = new VBox(6, title, progress, bounds, count, state);
        tile.setAlignment(Pos.TOP_CENTER);
        tile.setPrefWidth(150);
        workerPane.getChildren().add(tile);

        worker.setOnRunning(event -> state.setText("Running"));
        worker.setOnSucceeded(event -> {
            if (workers.contains(worker)) {
                state.setText("Done");
                finishedWorkers++;
                if (finishedWorkers == workers.size()) {
                    publishResults(limit);
                } else {
                    showRunningStatus();
                }
            }
        });
        worker.setOnFailed(event -> {
            if (workers.contains(worker)) {
                cancelSearch("Search failed.");
            }
        });
    }

    private void showRunningStatus() {
        statusLabel.setText("Searching. Finished threads: " + finishedWorkers + " of " + workers.size() + ".");
    }

    private void publishResults(int limit) {
        StringJoiner output = new StringJoiner(", ");
        int matchCount = 0;
        for (SearchTask worker : workers) {
            List<Integer> matches = worker.getValue();
            matchCount += matches.size();
            for (int match : matches) {
                output.add(Integer.toString(match));
            }
        }
        resultsArea.setText(output.toString());
        showCounts(matchCount, limit);
        statusLabel.setText("Complete. Checked all " + limit + " numbers from 1 to " + limit
                + ". Finished threads: " + finishedWorkers + " of " + workers.size() + ".");
        workers.clear();
        setRunning(false);
    }

    @FXML
    private void stopSearch() {
        cancelSearch("Stopped.");
    }

    private void cancelSearch(String status) {
        shutdown();
        workerPane.getChildren().clear();
        clearResults();
        statusLabel.setText(status);
        setRunning(false);
    }

    void shutdown() {
        for (SearchTask worker : workers) {
            worker.cancel(true);
        }
        workers.clear();
    }

    private void setRunning(boolean running) {
        upperLimitField.setDisable(running);
        threadCountField.setDisable(running);
        startButton.setDisable(running);
        stopButton.setDisable(!running);
    }

    private void clearResults() {
        resultsArea.clear();
        showCounts(0, 0);
    }

    private void showCounts(int matches, int total) {
        disariumCountLabel.setText("Disarium numbers: " + matches);
        nonDisariumCountLabel.setText("Non-Disarium numbers: " + (total - matches));
        totalCheckedLabel.setText("Total checked: " + total);
    }

    private static final class SearchTask extends Task<List<Integer>> {
        private final DisariumSearch.Range range;

        SearchTask(DisariumSearch.Range range) {
            this.range = range;
            updateProgress(0, range.size());
        }

        @Override
        protected List<Integer> call() {
            List<Integer> matches = new ArrayList<>();
            int checked = 0;
            int size = range.size();
            long lastUpdate = System.nanoTime();
            for (long candidate = range.start(); candidate <= range.end(); candidate++) {
                if (isCancelled()) {
                    break;
                }
                if (DisariumSearch.isDisarium((int) candidate)) {
                    matches.add((int) candidate);
                }
                checked++;
                if (checked == size || (checked % 256 == 0 && System.nanoTime() - lastUpdate >= 50_000_000L)) {
                    updateProgress(checked, size);
                    lastUpdate = System.nanoTime();
                }
            }
            return matches;
        }
    }
}
