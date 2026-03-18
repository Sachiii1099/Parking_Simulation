package core;
import model.Parking;
import model.enums.CellType;
import java.util.*;

public class Main {
    public static void main(String[] args){
        int rows,cols;


        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of rows:");
        rows= sc.nextInt();
        System.out.print("Enter number of columns");
        cols= sc.nextInt();

        CellType[][] layout= new CellType[rows][cols];

    System.out.println("Customize your parking");
    System.out.println("Enter as per the codes:\n 0--ROAD \n 1--SLOT \n 2--GATE \n 3--BLOCK\n 4--LIFT\n");
    for(int i=0;i<rows;i++){
        for(int j=0;j<cols;j++){
            int input= sc.nextInt();

            switch(input){
                case 0: layout[i][j]=CellType.ROAD; break;
                case 1: layout[i][j]=CellType.SLOT; break;
                case 2: layout[i][j]=CellType.GATE; break;
                case 3: layout[i][j]=CellType.BLOCK; break;
                case 4: layout[i][j]=CellType.LIFT; break;
                default: layout[i][j]=CellType.ROAD; break;
            }
        }
    }
    Parking p=new Parking(rows,cols,layout);
    if(p.getGates().isEmpty()){
        System.out.println("No gates are present, Please enter gates");

    }



    }

}
