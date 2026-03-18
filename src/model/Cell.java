package model;
import model.enums.CellType;
public class Cell {
    private int row;
    private int col;
    private CellType type;
    private boolean occupied;

    public Cell(int row,int col,CellType type){
        this.row= row;
        this.col=col;
        this.type = type;
    }

    public int getRow(){return row;}
    public int getCol(){return col;}
    public CellType getType(){return type;}
    public boolean isOccupied(){return occupied;}

    public void setType(CellType type){
        this.type=type;
    }
    public void setOccupied(boolean occupied){
        if(this.type==CellType.SLOT) {
            this.occupied = occupied;
        }
    }







}
