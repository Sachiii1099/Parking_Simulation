package core;
import model.Parking;
import model.Cell;
import model.enums.VehicleSize;
import model.enums.CellType;
import simulation.WorkingSimulation;

import java.util.*;

public class Main {
    public static void main(String[] args){
        int rows,cols;


        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of rows:");
        rows= sc.nextInt();
        System.out.print("Enter number of columns");
        cols= sc.nextInt();

        Parking p;
        do {
            Cell[][] grid = new Cell[rows][cols];

            System.out.println("Customize your parking");
            System.out.println("Enter as per the codes:\n 0--ROAD \n 1--SLOT  (enter size: 6=STANDARD, 7=LARGE) \n 2--GATE \n 3--BLOCK\n 4--LIFT\n");
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
            p=new Parking(rows,cols,grid);
            if(p.getGates().isEmpty()){
                System.out.println("No gates are present, Please enter gates");

            }
        }while(p.getGates().isEmpty());

        System.out.println("Enter the time parking is open");
        int clock=sc.nextInt();

        System.out.println("\n-=-=-=-=-=-=-=-=-=-WELCOME-=-=-=-=-=-=-==-=-=");
        WorkingSimulation sim=new WorkingSimulation(p);
        sim.printParking(p);
        for(int i=0;i<clock;i++){
            System.out.println("*******-------*******Clock"+(i+1)+"*********--------*********");
            sim.runClock();
        }


    }

}
