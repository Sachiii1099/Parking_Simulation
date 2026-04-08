package BasicBuildingBlocks;
import BasicBuildingBlocks.enums.CellType;
import java.util.*;

public class ParkingFloor {
    private int floorNumber;
    private  Cell[][] grid;

    private  int rows;


    private  int cols;
        private  List<Cell>gates;


    public ParkingFloor(int floorNumber, int rows, int cols, Cell[][] grid) {
             this.floorNumber = floorNumber;

        this.rows = rows;
        this.cols = cols;


            this.grid = grid;
            this.gates = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j].getType() == CellType.GATE) {
                    gates.add(grid[i][j]);
                }



                             }
                }
    }

    public int getFloorNumber() { return floorNumber; }


                          public Cell[][] getGrid() { return grid; }
                public int getRows() { return rows; }

        public int getCols() { return cols; }

    public List<Cell> getGates() { return gates; }
}


