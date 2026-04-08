package core;
import BasicBuildingBlocks.Parking;
import BasicBuildingBlocks.Cell;
import BasicBuildingBlocks.ParkingFloor;
import BasicBuildingBlocks.enums.VehicleSize;
import BasicBuildingBlocks.enums.CellType;
import logicOfSimulation.WorkingSimulation;

import java.util.*;

public class Main {
    public static void main(String[] args){
        int rows, cols, numFloors;
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of rows: ");
        rows = sc.nextInt();

        System.out.print("Enter number of columns: ");
        cols = sc.nextInt();




        System.out.print("Enter total number of floors: ");
        numFloors = sc.nextInt();

        List<ParkingFloor> allFloors = new ArrayList<>();


        boolean basementValid = false;
        do {
            Cell[][] grid = new Cell[rows][cols];
            System.out.println("Input for the ground floor...");


            System.out.println("Enter \n 0--ROAD \n 1--SLOT  (enter size: 6=STANDARD, 7=LARGE) \n 2--GATE \n 3--BLOCK\n 4--LIFT\n");

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int input = sc.nextInt();
                    switch (input) {




                        case 0:
                            grid[i][j] = new Cell(i, j, CellType.ROAD);
                            break;
                        case 1: {
                            int sizeEnter = sc.nextInt();
                            VehicleSize size;
                            if (sizeEnter == 7) {
                                size = VehicleSize.LARGE;
                            } else {
                                size = VehicleSize.STANDARD;





                            }
                            grid[i][j] = new Cell(i, j, CellType.SLOT, size);
                            break;
                        }
                        case 2:
                            grid[i][j] = new Cell(i, j, CellType.GATE);
                            break;
                        case 3:
                            grid[i][j] = new Cell(i, j, CellType.BLOCK);
                            break;
                        case 4:
                            grid[i][j] = new Cell(i, j, CellType.LIFT);
                            break;
                        default:
                            grid[i][j] = new Cell(i, j, CellType.ROAD);





                            break;
                    }
                }
            }


            boolean hasGate = false;
                     for(int i = 0; i < rows; i++) {
                for(int j = 0; j < cols; j++) {
                      if(grid[i][j].getType() == CellType.GATE) hasGate = true;
                }
            }

            if(!hasGate) {
                             System.out.println("No gates are present in basement, Please enter gates");
            } else {
                allFloors.add(new ParkingFloor(0, rows, cols, grid));
                           basementValid = true;






            }
        } while(!basementValid);


        if (numFloors > 1) {
            Cell[][] floor1Grid = new Cell[rows][cols];
            System.out.println("Input for the 1st floor ");



                 System.out.println("0--ROAD, 1--SLOT (6/7), 2--GATE, 3--BLOCK, 4--LIFT");
            for (int i = 0; i < rows; i++) {



                for (int j = 0; j < cols; j++) {
                    int input = sc.nextInt();
                    switch (input) {
                        case 0:
                            floor1Grid[i][j] = new Cell(i, j, CellType.ROAD);
                            break;
                        case 1: {
                            int sizeEnter = sc.nextInt();
                            VehicleSize size;
                            if (sizeEnter == 7) {
                                size = VehicleSize.LARGE;
                            } else {
                                size = VehicleSize.STANDARD;
                            }
                            floor1Grid[i][j] = new Cell(i, j, CellType.SLOT, size);
                            break;
                                        }
                                        case 2:
                                            floor1Grid[i][j] = new Cell(i, j, CellType.GATE);
                                            break;
                                        case 3:
                                            floor1Grid[i][j] = new Cell(i, j, CellType.BLOCK);
                                            break;
                                        case 4:
                                            floor1Grid[i][j] = new Cell(i, j, CellType.LIFT);
                                            break;
                        default:
                            floor1Grid[i][j] = new Cell(i, j, CellType.ROAD);
                            break;




                    }



                }

            }
            allFloors.add(new ParkingFloor(1, rows, cols, floor1Grid));

            // Copy Floor 1 layout for remaining floors
            for (int f = 2; f < numFloors; f++) {





                allFloors.add(new ParkingFloor(f, rows, cols, copyLayout(rows, cols, floor1Grid)));
            }
        }

        Parking p = new Parking(allFloors);

                    System.out.println("Enter the time parking is open");
                    int clock = sc.nextInt();

                    System.out.println("\nStarting Building Simulation-----------------");
                          WorkingSimulation sim = new WorkingSimulation(p);




                    for(int i = 0; i < clock; i++){
            System.out.println("Clock " + (i+1) + "----------------");
            sim.runClock();
        }
    }

    private static Cell[][] copyLayout(int rows, int cols, Cell[][] original) {
        Cell[][] copy = new Cell[rows][cols];




        for (int i = 0; i < rows; i++) {
             for (int j = 0; j < cols; j++) {
                                     copy[i][j] = new Cell(i, j, original[i][j].getType(), original[i][j].getSlotSize());
                            }






        }
        return copy;






    }
}