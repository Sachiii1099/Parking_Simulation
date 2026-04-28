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

    private static final int CELL     = 62;

            private static final int PAD      = 40;


            private static final int FLOOR_GAP = 50;

    private final Parking parking;


    private final SimulationController ctrl;
    private final Canvas canvas;

    public GridRenderer(Parking parking, SimulationController ctrl) {


        this.parking = parking;

        this.ctrl    = ctrl;

        int floors = parking.getTotalFloors();

                 int rows   = parking.getFloor(0).getRows();
         int cols   = parking.getFloor(0).getCols();


                    int w = floors * (cols * CELL + FLOOR_GAP) + PAD * 2;
                    int h = rows * CELL + PAD * 2 + 30;
                    canvas = new Canvas(w, h);
    }

    public Canvas getCanvas() { return canvas; }

    public void render() {
            GraphicsContext gc = canvas.getGraphicsContext2D();

                         gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                Map<Vehicle, Cell>    positions = ctrl.getCarPositions();


                Map<Vehicle, Integer> floorMap  = ctrl.getCarFloors();

        int offsetX = PAD;

            for (int f = 0; f < parking.getTotalFloors(); f++) {
                ParkingFloor floor = parking.getFloor(f);



                    gc.setFill(Color.web("#888888"));

                    gc.setFont(Font.font("Arial", 13));

                        gc.setTextAlign(TextAlignment.LEFT);
                        gc.fillText("── Floor " + f + " ──", offsetX, PAD - 12);


                                    for (int i = 0; i < floor.getRows(); i++) {

                                        for (int j = 0; j < floor.getCols(); j++) {

                                            drawCell(gc,
                                                    floor.getGrid()[i][j],


                                                    offsetX + j * CELL,
                                                    PAD     + i * CELL);
                                        }
                                    }


                        for (Map.Entry<Vehicle, Cell> e : positions.entrySet()) {
                            Vehicle car = e.getKey();
                            Cell    pos = e.getValue();
                            Integer cf  = floorMap.get(car);
                            if (cf != null && cf == f) {
                                drawCar(gc, car,
                                        offsetX + pos.getCol() * CELL,
                                        PAD     + pos.getRow() * CELL);
                            }
                        }

            offsetX += floor.getCols() * CELL + FLOOR_GAP;
            }
    }



    private void drawCell(GraphicsContext gc, Cell cell, int x, int y) {
            Color  bg    = Color.web("#2d2d2d");

            String label = "";

        switch (cell.getType()) {
            case ROAD:
                bg = Color.web("#2d2d2d");

                break;

            case BLOCK:
                bg = Color.web("#4a4a4a");
                label = "✖";

                break;
                 case GATE:
                        bg = Color.web("#e65100");
                        label = "G";
                break;
                case LIFT:
                    bg = Color.web("#6a1b9a");
                    label = "L";

                        break;
                case SLOT:
                    if (cell.isOccupied()) {
                        bg = Color.web("#263238");

                    } else {
                        bg = cell.getSlotSize() == VehicleSize.LARGE
                                ? Color.web("#1b5e20")

                                : Color.web("#0d3c55");

                        label = cell.getSlotSize() == VehicleSize.LARGE ? "SL" : "SS";
                    }
                    break;
        }


            gc.setFill(bg);

            gc.fillRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 8, 8);


                gc.setStroke(Color.web("#333333"));

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
        boolean isParked = ctrl.getParkedCars().containsKey(car);
        Color   color    = carColor(car);


        gc.setFill(Color.rgb(0, 0, 0, 0.35));
        gc.fillRoundRect(x + 11, y + 11, CELL - 18, CELL - 18, 9, 9);


        gc.setFill(color);
        gc.fillRoundRect(x + 9, y + 9, CELL - 18, CELL - 18, 9, 9);


        if (isParked) {
            gc.setStroke(Color.web("#FFD600"));
            gc.setLineWidth(2.5);
            gc.strokeRoundRect(x + 9, y + 9, CELL - 18, CELL - 18, 9, 9);
        }


        gc.setFill(Color.WHITE);

        gc.setFont(Font.font("Arial Bold", 10));

                         gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("#" + car.getId(),
                x + CELL / 2.0,
                y + CELL / 2.0 - 3);

            gc.setFont(Font.font("Arial", 8));

            gc.setFill(Color.web("#eeeeee"));

        gc.fillText(car.getType().name().substring(0, 3).toUpperCase(),
                x + CELL / 2.0,
                y + CELL / 2.0 + 8);
    }

    private Color carColor(Vehicle car) {
            switch (car.getType()) {
                case NORMAL:    return Color.web("#1565C0");


                        case VIP:       return Color.web("#F9A825");

                        case DISABLED:  return Color.web("#2E7D32");

                case AMBULANCE: return Color.web("#B71C1C");




                default:        return Color.GRAY;
            }
    }
}