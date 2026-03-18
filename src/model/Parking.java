package model;

import model.enums.CellType;

import java.util.ArrayList;
import java.util.List;

public class Parking {
    private Cell[][] grid;
    private int rows;
    private int cols;
    private List<Cell> gates;

    public Parking(int rows,int cols, CellType[][] layout){
        this.rows = rows;
        this.cols=cols;
         grid= new Cell[rows][cols];
         gates=new ArrayList<>();

         for(int i=0;i<rows;i++){
             for(int j=0;j<cols;j++){
                 Cell cell=new Cell(i,j,layout[i][j]);
                 grid[i][j]=cell;

                 if(layout[i][j]==CellType.GATE){
                     gates.add(cell);
                 }
             }
         }

    }
    public Cell[][] getGrid(){ return grid;}
    public List<Cell> getGates(){ return gates;}
    public int getRows(){ return rows;}
    public int getCols(){ return cols;}


}
