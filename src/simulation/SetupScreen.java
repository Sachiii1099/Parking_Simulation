package simulation;

import BasicBuildingBlocks.*;
import BasicBuildingBlocks.enums.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.*;

public class SetupScreen {

    private static final int CELL = 52;

                private int rows, cols, numFloors;
    private int editingFloor = 0;



                        private CellType   selectedType = CellType.ROAD;
                        private VehicleSize selectedSize = VehicleSize.STANDARD;

                        private CellType[][][]   typeGrid;
                        private VehicleSize[][][] sizeGrid;

    private Canvas          canvas;
    private GraphicsContext gc;
                    private Stage           stage;
    private Label           selectedLabel;



    public void show(Stage stage) {
        this.stage = stage;

        VBox root = new VBox(16);
        root.setPadding(new Insets(36));
                        root.setAlignment(Pos.CENTER);
                        root.setStyle("-fx-background-color:#1a1a2e;");




                Label title = new Label("🅿  Parking Simulator");
                             title.setStyle("-fx-text-fill:white;-fx-font-size:22px;-fx-font-weight:bold;");

                TextField rowsF   = field("5");

                        TextField colsF   = field("8");
                TextField floorsF = field("1");

                Button go = new Button("Build Layout  ");
        go.setStyle("-fx-background-color:#4CAF50;-fx-text-fill:white;"

                + "-fx-font-size:14px;-fx-padding:8 24;");

        go.setOnAction(e -> {
            try {
                          rows      = Math.max(2, Integer.parseInt(rowsF.getText().trim()));
                     cols      = Math.max(2, Integer.parseInt(colsF.getText().trim()));
                                numFloors = Math.max(1, Integer.parseInt(floorsF.getText().trim()));
                initGrids();

                showEditor();


            } catch (NumberFormatException ex) {

                     alert("Enter valid integers for rows, columns and floors.");
            }
        });

        root.getChildren().addAll(
                title,

                row("Rows :", rowsF),

                    row("Columns :", colsF),
                    row("Floors :", floorsF),
                go);

        stage.setScene(new Scene(root, 380, 300));

            stage.setTitle("Setup");
        stage.show();
    }



    private void initGrids() {
        typeGrid = new CellType[numFloors][rows][cols];

        sizeGrid = new VehicleSize[numFloors][rows][cols];
        for (int f = 0; f < numFloors; f++)
            for (int i = 0; i < rows; i++)

                for (int j = 0; j < cols; j++) {
                    typeGrid[f][i][j] = CellType.ROAD;

                    sizeGrid[f][i][j] = VehicleSize.STANDARD;
                }
    }

    private void showEditor() {
        BorderPane root = new BorderPane();

             root.setStyle("-fx-background-color:#1a1a2e;");

                root.setTop(buildToolbar());

                            canvas = new Canvas(cols * CELL + 4, rows * CELL + 4);

                            gc     = canvas.getGraphicsContext2D();

                            drawGrid();

                            canvas.setOnMousePressed(e  -> paint(e.getX(), e.getY()));


                    canvas.setOnMouseDragged(e  -> paint(e.getX(), e.getY()));

        ScrollPane sp = new ScrollPane(canvas);


                     sp.setStyle("-fx-background:#1a1a2e;-fx-background-color:#1a1a2e;");



        root.setCenter(sp);



        root.setBottom(buildBottomBar());

        double w = Math.min(cols * CELL + 340, 1100);

                         double h = Math.min(rows * CELL + 180, 780);






        stage.setScene(new Scene(root, w, h));


                  stage.setTitle("Floor " + editingFloor + " — Editor");
    }

    private void paint(double mx, double my) {


           int c = (int)(mx / CELL);


        int r = (int)(my / CELL);
                        if (r < 0 || r >= rows || c < 0 || c >= cols) return;



                        typeGrid[editingFloor][r][c] = selectedType;



        sizeGrid[editingFloor][r][c] = selectedSize;


        drawGrid();
    }



    private HBox buildToolbar() {


                    HBox bar = new HBox(8);


                    bar.setPadding(new Insets(10));



        bar.setAlignment(Pos.CENTER_LEFT);


        bar.setStyle("-fx-background-color:#16213e;");

        selectedLabel = new Label("Selected: ROAD");


        selectedLabel.setStyle("-fx-text-fill:#aaa;-fx-font-size:12px;");

        bar.getChildren().addAll(
                tBtn("Road",    CellType.ROAD,  null,               "#555"),



                        tBtn("Slot S",  CellType.SLOT,  VehicleSize.STANDARD,"#0d3c55"),



                        tBtn("Slot L",  CellType.SLOT,  VehicleSize.LARGE,   "#1b5e20"),



                        tBtn("Gate",    CellType.GATE,  null,               "#e65100"),



                tBtn("Block",   CellType.BLOCK, null,               "#424242"),



                tBtn("Lift",    CellType.LIFT,  null,               "#6a1b9a"),



                new Separator(),
                selectedLabel
        );


        if (numFloors > 1) {

            bar.getChildren().add(new Separator());



            for (int f = 0; f < Math.min(numFloors, 5); f++) {
                int ff = f;



                Button fb = new Button("F" + f);



                         fb.setStyle("-fx-background-color:#333;-fx-text-fill:white;");
                fb.setOnAction(e -> {



                               editingFloor = ff;
                    drawGrid();
                    stage.setTitle("Floor " + ff + " — Editor");
                });
                bar.getChildren().add(fb);
            }
        }

        return bar;
    }

    private Button tBtn(String label, CellType type, VehicleSize size, String bg) {
        Button b = new Button(label);
        b.setStyle("-fx-background-color:" + bg
                + ";-fx-text-fill:white;-fx-padding:5 10;"



                           + "-fx-border-radius:4;-fx-background-radius:4;");
        b.setOnAction(e -> {
            selectedType = type;
            selectedSize = (size != null) ? size : VehicleSize.STANDARD;
            selectedLabel.setText("Selected: " + label);
        });
        return b;
    }

    // ─────────────────────────────────────────────
    //  Bottom bar
    // ─────────────────────────────────────────────

    private HBox buildBottomBar() {
        HBox bar = new HBox(12);
                        bar.setPadding(new Insets(10));
                        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setStyle("-fx-background-color:#16213e;");




        Label hint = new Label(
                "Ground floor needs ≥1 Gate.  Upper floors need ≥1 Gate + roads connecting to slots.");
        hint.setStyle("-fx-text-fill:#888;-fx-font-size:11px;");

        Button launch = new Button("▶  Launch Simulation");
                    launch.setStyle("-fx-background-color:#1565C0;-fx-text-fill:white;"
                            + "-fx-font-size:13px;-fx-padding:7 18;");
        launch.setOnAction(e -> launchSim());

        bar.getChildren().addAll(hint, launch);
        return bar;
    }

    // ─────────────────────────────────────────────
    //  Validation + launch
    // ─────────────────────────────────────────────

    private void launchSim() {

        boolean hasGate = false;
        for (int i = 0; i < rows && !hasGate; i++)
            for (int j = 0; j < cols && !hasGate; j++)
                if (typeGrid[0][i][j] == CellType.GATE) hasGate = true;
        if (!hasGate) { alert("Ground floor needs at least one GATE!"); return; }

        // Validate upper floors each have at least one gate
        for (int f = 1; f < numFloors; f++) {
            boolean ug = false;
            for (int i = 0; i < rows && !ug; i++)
                for (int j = 0; j < cols && !ug; j++)
                    if (typeGrid[f][i][j] == CellType.GATE) ug = true;
            if (!ug) {
                alert("Floor " + f + " needs at least one GATE\n"
                        + "(cars enter upper floors through gates).");
                return;
            }
        }

        // Build Parking
        List<ParkingFloor> floors = new ArrayList<>();
        floors.add(new ParkingFloor(0, rows, cols, buildGrid(0)));

        if (numFloors > 1) {
            Cell[][] g1 = buildGrid(1);
            floors.add(new ParkingFloor(1, rows, cols, g1));
            for (int f = 2; f < numFloors; f++)
                floors.add(new ParkingFloor(f, rows, cols, copyGrid(g1)));
        }

        Parking parking = new Parking(floors);
        Stage simStage  = new Stage();
        new SimulationApp().startWithParking(simStage, parking);
        stage.close();
    }

    private Cell[][] buildGrid(int f) {
        Cell[][] g = new Cell[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                CellType    t = typeGrid[f][i][j];
                VehicleSize s = sizeGrid[f][i][j];
                g[i][j] = (t == CellType.SLOT)
                        ? new Cell(i, j, t, s)
                        : new Cell(i, j, t);
            }
        return g;
    }

    private Cell[][] copyGrid(Cell[][] src) {
        Cell[][] c = new Cell[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                c[i][j] = new Cell(i, j, src[i][j].getType(), src[i][j].getSlotSize());
        return c;
    }

    // ─────────────────────────────────────────────
    //  Grid drawing
    // ─────────────────────────────────────────────

    private void drawGrid() {
        gc.setFill(Color.web("#1a1a2e"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellType    t = typeGrid[editingFloor][i][j];
                VehicleSize s = sizeGrid[editingFloor][i][j];
                int x = j * CELL, y = i * CELL;

                Color  bg = cellColor(t, s);
                String lb = cellLabel(t, s);

                gc.setFill(bg);
                gc.fillRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 8, 8);
                gc.setStroke(Color.web("#333"));
                gc.setLineWidth(1);
                gc.strokeRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 8, 8);

                if (!lb.isEmpty()) {
                    gc.setFill(Color.web("#dddddd"));
                    gc.setFont(Font.font("Arial", 11));
                    gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
                    gc.fillText(lb, x + CELL / 2.0, y + CELL / 2.0 + 4);
                }
            }
        }
    }

    private Color cellColor(CellType t, VehicleSize s) {
        switch (t) {
            case ROAD:  return Color.web("#2d2d2d");
            case BLOCK: return Color.web("#424242");


                    case GATE:  return Color.web("#e65100");

                    case LIFT:  return Color.web("#6a1b9a");
                    case SLOT:  return s == VehicleSize.LARGE
                    ? Color.web("#1b5e20")
                    : Color.web("#0d3c55");



                    default:    return Color.DARKGRAY;
        }
    }

    private String cellLabel(CellType t, VehicleSize s) {
        switch (t) {
            case BLOCK: return "✖";
                case GATE:  return "G";

                    case LIFT:  return "L";


                    case SLOT:  return s == VehicleSize.LARGE ? "SL" : "SS";



                         default:    return "";
        }
    }



    private TextField field(String def) {




        TextField f = new TextField(def);
                        f.setStyle("-fx-background-color:#2d2d2d;-fx-text-fill:white;"

                                + "-fx-border-color:#555;-fx-border-radius:4;");



                        f.setPrefWidth(70);
                    return f;
    }

    private HBox row(String labelText, TextField tf) {
            Label l = new Label(labelText);
            l.setStyle("-fx-text-fill:#aaa;-fx-font-size:13px;");

                l.setMinWidth(80);

                HBox h = new HBox(10, l, tf);
                h.setAlignment(Pos.CENTER);


                return h;
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);


             a.setHeaderText(null);
                    a.showAndWait();
    }
}