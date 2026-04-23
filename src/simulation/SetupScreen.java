package simulation;

import BasicBuildingBlocks.*;
import BasicBuildingBlocks.enums.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

import java.util.ArrayList;
import java.util.List;

public class SetupScreen {

    private static final int CELL = 50;

    private int rows, cols, numFloors;
    private int currentFloor = 0;
    private CellType selectedType = CellType.ROAD;
    private VehicleSize selectedSize = VehicleSize.STANDARD;

    // [floor][row][col]
    private CellType[][][] typeGrid;
    private VehicleSize[][][] sizeGrid;

    private Canvas canvas;
    private GraphicsContext gc;
    private Stage stage;

    public void show(Stage stage) {
        this.stage = stage;

        // --- Input screen ---
        VBox inputBox = new VBox(12);
        inputBox.setPadding(new Insets(30));
        inputBox.setStyle("-fx-background-color: #1e1e1e;");
        inputBox.setAlignment(Pos.CENTER);

        Label title = styled("🅿 Parking Simulator Setup", 20, "#ffffff");
        TextField rowsField = new TextField("5");
        TextField colsField = new TextField("7");
        TextField floorsField = new TextField("1");

        styleField(rowsField, "Rows");
        styleField(colsField, "Cols");
        styleField(floorsField, "Floors");

        Button startBtn = new Button("Build Layout ▶");
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size:14px; -fx-padding: 8 20;");

        startBtn.setOnAction(e -> {
            try {
                rows = Integer.parseInt(rowsField.getText().trim());
                cols = Integer.parseInt(colsField.getText().trim());
                numFloors = Integer.parseInt(floorsField.getText().trim());
                if (rows < 2 || cols < 2 || numFloors < 1) throw new Exception();
                initGrids();
                showEditor();
            } catch (Exception ex) {
                showAlert("Please enter valid numbers (rows/cols >= 2, floors >= 1)");
            }
        });

        inputBox.getChildren().addAll(title,
                labeled("Number of Rows:", rowsField),
                labeled("Number of Columns:", colsField),
                labeled("Number of Floors:", floorsField),
                startBtn);

        stage.setScene(new Scene(inputBox, 400, 320));
        stage.setTitle("Parking Simulator - Setup");
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
        root.setStyle("-fx-background-color: #1e1e1e;");

        // --- Toolbar top ---
        HBox toolbar = buildToolbar();
        root.setTop(toolbar);

        // --- Canvas center ---
        canvas = new Canvas(cols * CELL + 2, rows * CELL + 2);
        gc = canvas.getGraphicsContext2D();
        drawGrid();

        canvas.setOnMouseClicked(e -> {
            int col = (int)(e.getX() / CELL);
            int row = (int)(e.getY() / CELL);
            if (row >= 0 && row < rows && col >= 0 && col < cols) {
                typeGrid[currentFloor][row][col] = selectedType;
                sizeGrid[currentFloor][row][col] = selectedSize;
                drawGrid();
            }
        });

        // Also support drag-painting
        canvas.setOnMouseDragged(e -> {
            int col = (int)(e.getX() / CELL);
            int row = (int)(e.getY() / CELL);
            if (row >= 0 && row < rows && col >= 0 && col < cols) {
                typeGrid[currentFloor][row][col] = selectedType;
                sizeGrid[currentFloor][row][col] = selectedSize;
                drawGrid();
            }
        });

        ScrollPane scroll = new ScrollPane(canvas);
        scroll.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        root.setCenter(scroll);

        // --- Bottom bar ---
        HBox bottomBar = buildBottomBar();
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, Math.min(cols * CELL + 300, 1000), rows * CELL + 160);
        stage.setScene(scene);
        stage.setTitle("Floor " + currentFloor + " Editor");
    }

    private HBox buildToolbar() {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(10));
        bar.setStyle("-fx-background-color: #2d2d2d;");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = styled("Select Type:", 13, "#aaaaaa");
        bar.getChildren().add(title);

        // Cell type buttons
        bar.getChildren().add(typeBtn("Road", CellType.ROAD, "#2d2d2d", "#888888"));
        bar.getChildren().add(typeBtn("Slot S", CellType.SLOT, "#1a2a3a", "#64B5F6"));
        bar.getChildren().add(typeBtn("Slot L", CellType.SLOT, "#1a3a2a", "#81C784"));
        bar.getChildren().add(typeBtn("Gate", CellType.GATE, "#FF9800", "#ffffff"));
        bar.getChildren().add(typeBtn("Block", CellType.BLOCK, "#555555", "#ffffff"));
        bar.getChildren().add(typeBtn("Lift", CellType.LIFT, "#9C27B0", "#ffffff"));

        // Floor tabs if multi-floor
        if (numFloors > 1) {
            Separator sep = new Separator();
            sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
            bar.getChildren().add(sep);
            bar.getChildren().add(styled("Floor:", 13, "#aaaaaa"));
            for (int f = 0; f < Math.min(numFloors, 2); f++) {
                int fCopy = f;
                Button fb = new Button("Floor " + f);
                fb.setStyle("-fx-background-color: " + (f == 0 ? "#4CAF50" : "#444") + "; -fx-text-fill: white;");
                fb.setOnAction(e -> {
                    currentFloor = fCopy;
                    drawGrid();
                    stage.setTitle("Floor " + currentFloor + " Editor");
                });
                bar.getChildren().add(fb);
            }
        }

        return bar;
    }

    private Button typeBtn(String label, CellType type, String bg, String fg) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg +
                "; -fx-border-color: #666; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 5 10;");
        btn.setOnAction(e -> {
            selectedType = type;
            selectedSize = label.equals("Slot L") ? VehicleSize.LARGE : VehicleSize.STANDARD;
        });
        return btn;
    }

    private HBox buildBottomBar() {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(10));
        bar.setStyle("-fx-background-color: #2d2d2d;");
        bar.setAlignment(Pos.CENTER_RIGHT);

        Label hint = styled("Ground floor must have at least 1 Gate. Upper floors need Gates or Lifts.", 11, "#888888");
        Button launchBtn = new Button("▶ Launch Simulation");
        launchBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size:13px; -fx-padding: 7 18;");

        launchBtn.setOnAction(e -> {
            if (!validateAndLaunch()) return;
        });

        bar.getChildren().addAll(hint, launchBtn);
        return bar;
    }

    private boolean validateAndLaunch() {
        // Check ground floor has gate
        boolean hasGate = false;
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (typeGrid[0][i][j] == CellType.GATE) hasGate = true;

        if (!hasGate) {
            showAlert("Ground floor must have at least one GATE!");
            return false;
        }

        // Build Parking object
        List<ParkingFloor> floors = new ArrayList<>();

        // Ground floor
        Cell[][] g0 = buildCellGrid(0);
        floors.add(new ParkingFloor(0, rows, cols, g0));

        if (numFloors > 1) {
            // Floor 1 custom
            Cell[][] g1 = buildCellGrid(1);
            floors.add(new ParkingFloor(1, rows, cols, g1));

            // Remaining floors copy floor 1
            for (int f = 2; f < numFloors; f++) {
                floors.add(new ParkingFloor(f, rows, cols, copyLayout(g1)));
            }
        }

        Parking parking = new Parking(floors);

        // Launch simulation window
        Stage simStage = new Stage();
        SimulationApp app = new SimulationApp();
        app.startWithParking(simStage, parking);
        stage.close();
        return true;
    }

    private Cell[][] buildCellGrid(int floor) {
        Cell[][] grid = new Cell[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                CellType t = typeGrid[floor][i][j];
                VehicleSize s = sizeGrid[floor][i][j];
                grid[i][j] = (t == CellType.SLOT)
                        ? new Cell(i, j, t, s)
                        : new Cell(i, j, t);
            }
        return grid;
    }

    private Cell[][] copyLayout(Cell[][] original) {
        Cell[][] copy = new Cell[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                copy[i][j] = new Cell(i, j, original[i][j].getType(), original[i][j].getSlotSize());
        return copy;
    }

    private void drawGrid() {
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                CellType t = typeGrid[currentFloor][i][j];
                VehicleSize s = sizeGrid[currentFloor][i][j];
                int x = j * CELL, y = i * CELL;

                Color bg;
                String label = "";
                switch (t) {
                    case ROAD:  bg = Color.web("#2d2d2d"); label = ""; break;
                    case BLOCK: bg = Color.web("#555555"); label = "✖"; break;
                    case GATE:  bg = Color.web("#FF9800"); label = "G"; break;
                    case LIFT:  bg = Color.web("#9C27B0"); label = "L"; break;
                    case SLOT:
                        bg = s == VehicleSize.LARGE ? Color.web("#1a3a2a") : Color.web("#1a2a3a");
                        label = s == VehicleSize.LARGE ? "SL" : "SS";
                        break;
                    default: bg = Color.DARKGRAY; break;
                }

                gc.setFill(bg);
                gc.fillRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 8, 8);
                gc.setStroke(Color.web("#444444"));
                gc.setLineWidth(1);
                gc.strokeRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 8, 8);

                if (!label.isEmpty()) {
                    gc.setFill(Color.web("#cccccc"));
                    gc.setFont(Font.font("Arial", 10));
                    gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
                    gc.fillText(label, x + CELL / 2.0, y + CELL / 2.0 + 4);
                }
            }
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private Label styled(String text, int size, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;");
        return l;
    }

    private HBox labeled(String labelText, TextField field) {
        Label l = new Label(labelText);
        l.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        field.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-border-color: #555; -fx-border-radius: 4;");
        field.setPrefWidth(80);
        HBox box = new HBox(10, l, field);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void styleField(TextField f, String prompt) {
        f.setPromptText(prompt);
        f.setStyle("-fx-background-color: #2d2d2d; -fx-text-fill: white; -fx-border-color: #555;");
        f.setPrefWidth(80);
    }
}