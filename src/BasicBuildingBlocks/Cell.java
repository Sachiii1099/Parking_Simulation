package BasicBuildingBlocks;

import BasicBuildingBlocks.enums.CellType;
import BasicBuildingBlocks.enums.VehicleSize;
import BasicBuildingBlocks.enums.VehicleType;

public class Cell {
    private int row;
    private int col;
    private CellType type;
    private VehicleSize slotSize;




    private Vehicle occupant;

    public Cell(int row, int col, CellType type) {
                this.row = row;
                this.col = col;
                this.type = type;
                this.slotSize = VehicleSize.STANDARD;
    }

    public Cell(int row, int col, CellType type, VehicleSize slotSize) {
                    this.row = row;
                    this.col = col;


                    this.type = type;
                    this.slotSize = slotSize;
    }

    public int getRow()              { return row; }
    public int getCol()              { return col; }
    public CellType getType()        { return type; }
    public VehicleSize getSlotSize() { return slotSize; }
    public Vehicle getOccupant()     { return occupant; }


    public boolean hasAnyCar()       { return occupant != null; }


    public boolean isOccupied() {
        return type == CellType.SLOT && occupant != null;
    }

    public void setType(CellType type) {
        this.type = type;
    }


    public void enter(Vehicle car) {
        this.occupant = car;
    }


    public void leave() {
        this.occupant = null;
    }


    public void park(Vehicle car) {
        if (this.type != CellType.SLOT) {
            throw new IllegalStateException(
                    "Cannot park on non-slot cell [" + row + "," + col + "]");
        }
        this.occupant = car;
    }

    public void unpark() {
        if (this.type != CellType.SLOT) {
            throw new IllegalStateException(
                    "Cannot unpark non-slot cell [" + row + "," + col + "]");
        }
        this.occupant = null;
    }


    public void parking(Vehicle car) { park(car); }


    public void setOccupied(boolean val) {
        if (!val) unpark();
    }

    public VehicleType getparkedVehicle() {
        return occupant != null ? occupant.getType() : null;
    }
}