package model;

import model.enums.CellType;
import model.enums.VehicleSize;

import java.util.ArrayList;
import java.util.List;

public class Parking {
    private Cell[][] grid;
    private int rows;
    private int cols;
    private List<Cell> gates;

    public Parking(int rows,int cols, Cell[][] grid){
        this.rows = rows;
        this.cols=cols;
        this.grid=grid;
         gates=new ArrayList<>();

         for(int i=0;i<rows;i++){
             for(int j=0;j<cols;j++){
                 if(grid[i][j].getType()==CellType.GATE){
                     gates.add(grid[i][j]);
                 }
             }
         }

    }
    public Cell[][] getGrid(){ return grid;}
    public List<Cell> getGates(){ return gates;}
    public int getRows(){ return rows;}
    public int getCols(){ return cols;}


}
