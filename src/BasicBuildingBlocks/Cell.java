package model;
import model.enums.CellType;
import model.enums.VehicleSize;
import model.enums.VehicleType;


public class Cell {
    private int row;
            private int col;
            private CellType type;

            private boolean occupied;
            private VehicleSize slotSize;


    private VehicleType parkedVehicle;


    public Cell(int row,int col,CellType type){
        this.row= row;
                this.col=col;
                this.type = type;
        this.slotSize=VehicleSize.STANDARD;


    }
    public Cell(int row,int col,CellType type,VehicleSize slotSize){
        this.row= row;
                                         this.col=col;
        this.type = type;
            this.slotSize=slotSize;

    }

    public int getRow(){return row;}

              public int getCol(){return col;}
                public CellType getType(){return type;}


    public boolean isOccupied(){return occupied;}
        public VehicleSize getSlotSize(){return slotSize;}





    public void setType(CellType type){
        this.type=type;
    }
          public void setOccupied(boolean occupied){
        if(this.type==CellType.SLOT) {
            this.occupied = occupied;
                }else {
            throw       new IllegalStateException("Occupied is checked on a wrong cell which is ["+row+"] ["+col+"].");
        }
    }
    public void parking(Vehicle car){
        if(this.type==CellType.SLOT){
                 this.occupied=true;
            this.parkedVehicle=car.getType();
        } else {
            throw new IllegalStateException(
                        "Cannot park on non-slot cell ["+row+","+col+"]");
        }
    }
    public       VehicleType getparkedVehicle(){
        return parkedVehicle;
    }







}
