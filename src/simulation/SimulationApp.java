package simulation;

import BasicBuildingBlocks.Parking;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SimulationApp extends Application {

    @Override
    public void start(Stage stage) {
        SetupScreen setup = new SetupScreen();
        setup.show(stage);
    }

    public void startWithParking(Stage stage, Parking parking) {
        SimulationController controller = new SimulationController(parking);
        GridRenderer renderer = new GridRenderer(parking, controller);

        Button stepBtn = new Button("▶ Next Tick");
        Button autoBtn = new Button("⏩ Auto Run");
        Button stopBtn = new Button("⏹ Stop");
        Label clockLabel = new Label("Tick: 0");

        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(800),
                        e -> {
                            controller.runTick();
                            renderer.render();
                            clockLabel.setText("Tick: " + controller.getClock());
                        }
                )
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);

        stepBtn.setOnAction(e -> {
            controller.runTick();
            renderer.render();
            clockLabel.setText("Tick: " + controller.getClock());
        });
        autoBtn.setOnAction(e -> timeline.play());
        stopBtn.setOnAction(e -> timeline.stop());

        clockLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        stepBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size:13px;");
        autoBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size:13px;");
        stopBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size:13px;");

        // Legend
        VBox legend = buildLegend();

        VBox controls = new VBox(12, clockLabel, stepBtn, autoBtn, stopBtn, legend);
        controls.setPadding(new Insets(12));
        controls.setStyle("-fx-background-color: #1e1e1e;");

        ScrollPane scrollPane = new ScrollPane(renderer.getCanvas());
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);

        HBox root = new HBox(10, scrollPane, controls);
        root.setStyle("-fx-background-color: #1e1e1e;");
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root);
        stage.setTitle("Parking Simulation");
        stage.setScene(scene);
        stage.show();

        renderer.render();
    }

    private VBox buildLegend() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10, 0, 0, 0));
        box.getChildren().add(legendLabel("— Legend —", "#888888"));
        box.getChildren().add(legendLabel("🔵 Normal Car", "#2196F3"));
        box.getChildren().add(legendLabel("🟡 VIP Car", "#FFC107"));
        box.getChildren().add(legendLabel("🟢 Disabled Car", "#4CAF50"));
        box.getChildren().add(legendLabel("🔴 Ambulance", "#f44336"));
        box.getChildren().add(legendLabel("🟠 Gate", "#FF9800"));
        box.getChildren().add(legendLabel("🟣 Lift", "#9C27B0"));
        box.getChildren().add(legendLabel("SS = Standard Slot", "#64B5F6"));
        box.getChildren().add(legendLabel("SL = Large Slot", "#81C784"));
        box.getChildren().add(legendLabel("Yellow ring = Parked", "#FFF176"));
        return box;
    }

    private Label legendLabel(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px;");
        return l;
    }

    public static void main(String[] args) {
        launch(args);
    }
}