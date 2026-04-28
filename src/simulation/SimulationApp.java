package simulation;

import BasicBuildingBlocks.Parking;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class SimulationApp extends Application {

    @Override
    public void start(Stage stage) {
        new SetupScreen().show(stage);
    }

    public void startWithParking(Stage stage, Parking parking) {
        SimulationController controller = new SimulationController(parking);
        GridRenderer renderer           = new GridRenderer(parking, controller);

        Button stepBtn  = new Button("▶ Next Tick");
        Button autoBtn  = new Button("⏩ Auto Run");
        Button stopBtn  = new Button("⏹ Stop");
        Label  tickLbl  = new Label("Tick: 0");
        Label  statsLbl = new Label("");

                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                        new javafx.animation.KeyFrame(
                                javafx.util.Duration.millis(700),
                                e -> {
                                    controller.runTick();


                                    renderer.render();
                                    tickLbl.setText("Tick: " + controller.getClock());
                                    statsLbl.setText(buildStats(controller));
                                }
                        )
                );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);

        stepBtn.setOnAction(e -> {
            controller.runTick();
            renderer.render();



            tickLbl.setText("Tick: " + controller.getClock());
            statsLbl.setText(buildStats(controller));
        });
        autoBtn.setOnAction(e -> timeline.play());
        stopBtn.setOnAction(e -> timeline.stop());

        style(tickLbl,  "white",   14, true);
                    style(statsLbl, "#aaaaaa", 11, false);
                    styleBtn(stepBtn, "#4CAF50");
                    styleBtn(autoBtn, "#2196F3");
        styleBtn(stopBtn, "#f44336");

        VBox controls = new VBox(10,
                tickLbl, stepBtn, autoBtn, stopBtn,
                new Separator(),
                            buildLegend(),
                new Separator(),
                statsLbl);
        controls.setPadding(new Insets(12));
                        controls.setMinWidth(170);
                        controls.setStyle("-fx-background-color: #1e1e1e;");

                        ScrollPane scroll = new ScrollPane(renderer.getCanvas());


                        scroll.setStyle("-fx-background:#1e1e1e; -fx-background-color:#1e1e1e;");
                        scroll.setPannable(true);

                    HBox root = new HBox(10, scroll, controls);
                    root.setStyle("-fx-background-color: #1e1e1e;");
                    root.setPadding(new Insets(10));

        stage.setScene(new Scene(root));



        stage.setTitle("Parking Simulation");
        stage.show();
        renderer.render();
    }

    private String buildStats(SimulationController c) {
        return "Moving cars : " + c.getCarPositions().size()
                + "\nParked cars : " + c.getParkedCars().size();
    }

    private VBox buildLegend() {
        VBox b = new VBox(5);
        b.getChildren().add(lbl("── Legend ──", "#888888", 12, true));
                b.getChildren().add(lbl("🔵 Normal",    "#2196F3", 11, false));
                b.getChildren().add(lbl("🟡 VIP",       "#FFC107", 11, false));
                            b.getChildren().add(lbl("🟢 Disabled",  "#4CAF50", 11, false));
                            b.getChildren().add(lbl("🔴 Ambulance", "#f44336", 11, false));
                            b.getChildren().add(lbl("🟠 Gate",      "#FF9800", 11, false));
                b.getChildren().add(lbl("🟣 Lift",      "#9C27B0", 11, false));
                b.getChildren().add(lbl("SS Std slot",  "#64B5F6", 11, false));
        b.getChildren().add(lbl("SL Lrg slot",  "#81C784", 11, false));
        b.getChildren().add(lbl("★ = Parked",   "#FFF176", 11, false));
        return b;
    }

    private Label lbl(String t, String c, int sz, boolean bold) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:" + c + ";-fx-font-size:" + sz
                + "px;" + (bold ? "-fx-font-weight:bold;" : ""));
        return l;
    }

    private void style(Label l, String color, int size, boolean bold) {
        l.setStyle("-fx-text-fill:" + color + ";-fx-font-size:" + size
                + "px;" + (bold ? "-fx-font-weight:bold;" : ""));
    }

    private void styleBtn(Button b, String color) {
        b.setStyle("-fx-background-color:" + color
                + ";-fx-text-fill:white;-fx-font-size:13px;");
        b.setMaxWidth(Double.MAX_VALUE);
    }

    public static void main(String[] args) { launch(args); }
}