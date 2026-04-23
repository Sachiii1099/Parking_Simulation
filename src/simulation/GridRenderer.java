package simulation;

import BasicBuildingBlocks.*;
import BasicBuildingBlocks.enums.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Map;

public class GridRenderer {

    private static final int CELL = 60;
    private static final int PADDING = 40;
    private static final int FLOOR_GAP = 60;

    private final Parking parking;
    private final SimulationController controller;
    private final Canvas canvas;

    public GridRenderer(Parking parking, SimulationController controller) {
        this.parking = parking;
        this.controller = controller;

        int floors = parking.getTotalFloors();
        int rows = parking.getFloor(0).getRows();
        int cols = parking.getFloor(0).getCols();

        // 🔥 HORIZONTAL layout
        int width = floors * (cols * CELL + FLOOR_GAP) + PADDING * 2;
        int height = rows * CELL + PADDING * 2 + 40;

        canvas = new Canvas(width, height);
    }

    public Canvas getCanvas() { return canvas; }

    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        Map<Vehicle, Cell> positions = controller.getCarPositions();
        Map<Vehicle, Integer> floorMap = controller.getCarFloors();

        int floorOffsetX = PADDING;

        for (int f = 0; f < parking.getTotalFloors(); f++) {
            ParkingFloor floor = parking.getFloor(f);

            // Floor title
            gc.setFill(Color.web("#888888"));
            gc.setFont(Font.font("Arial", 13));
            gc.fillText("── Floor " + f + " ──", floorOffsetX, PADDING - 10);

            // Draw grid
            for (int i = 0; i < floor.getRows(); i++) {
                for (int j = 0; j < floor.getCols(); j++) {
                    Cell cell = floor.getGrid()[i][j];

                    int x = floorOffsetX + j * CELL;
                    int y = PADDING + i * CELL;

                    drawCell(gc, cell, x, y);
                }
            }

            // Draw cars on this floor
            for (Map.Entry<Vehicle, Cell> entry : positions.entrySet()) {
                Vehicle car = entry.getKey();
                Cell pos = entry.getValue();

                if (floorMap.get(car) == f) {
                    int x = floorOffsetX + pos.getCol() * CELL;
                    int y = PADDING + pos.getRow() * CELL;
                    drawCar(gc, car, x, y);
                }
            }

            // Move to next floor horizontally
            floorOffsetX += floor.getCols() * CELL + FLOOR_GAP;
        }
    }

    private void drawCell(GraphicsContext gc, Cell cell, int x, int y) {
        Color bg;
        String label = "";

        switch (cell.getType()) {
            case ROAD:  bg = Color.web("#2d2d2d"); break;
            case BLOCK: bg = Color.web("#555555"); label = "✖"; break;
            case GATE:  bg = Color.web("#FF9800"); label = "G"; break;
            case LIFT:  bg = Color.web("#9C27B0"); label = "L"; break;
            case SLOT:
                if (cell.isOccupied()) {
                    bg = Color.web("#37474f");
                } else {
                    bg = cell.getSlotSize() == VehicleSize.LARGE
                            ? Color.web("#1a3a2a") : Color.web("#1a2a3a");
                    label = cell.getSlotSize() == VehicleSize.LARGE ? "SL" : "SS";
                }
                break;
            default: bg = Color.DARKGRAY;
        }

        gc.setFill(bg);
        gc.fillRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 8, 8);
        gc.setStroke(Color.web("#444444"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 8, 8);

        if (!label.isEmpty()) {
            gc.setFill(Color.web("#aaaaaa"));
            gc.setFont(Font.font("Arial", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(label, x + CELL / 2.0, y + CELL / 2.0 + 4);
        }
    }

    private void drawCar(GraphicsContext gc, Vehicle car, int x, int y) {
        Color color = getCarColor(car);
        boolean isParked = controller.getParkedCars().containsKey(car);

        gc.setFill(color);
        gc.fillRoundRect(x + 8, y + 8, CELL - 16, CELL - 16, 10, 10);

        if (isParked) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2.5);
            gc.strokeRoundRect(x + 8, y + 8, CELL - 16, CELL - 16, 10, 10);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial Bold", 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("#" + car.getId(), x + CELL / 2.0, y + CELL / 2.0 - 2);

        gc.setFont(Font.font("Arial", 9));
        gc.setFill(Color.web("#eeeeee"));
        gc.fillText(car.getType().name().substring(0, 3), x + CELL / 2.0, y + CELL / 2.0 + 9);
    }

    private Color getCarColor(Vehicle car) {
        switch (car.getType()) {
            case NORMAL:    return Color.web("#2196F3");
            case VIP:       return Color.web("#FFC107");
            case DISABLED:  return Color.web("#4CAF50");
            case AMBULANCE: return Color.web("#f44336");
            default:        return Color.GRAY;
        }
    }
}