package service;

import model.Cell;
import model.Parking;
import model.Vehicle;
import model.enums.VehicleSize;
import model.enums.CellType;
import model.enums.VehicleType;
import java.util.*;


public class SlotAllocation {
    private ParkingService parkingService= new ParkingService();

    public Cell slotAllocate(Parking area,Vehicle car,Cell gate){
        Cell best=null;

        int minDist=Integer.MAX_VALUE;
        for(int i=0;i<area.getRows();i++){
            for(int j=0;j<area.getCols();j++){
                Cell cell =area.getGrid()[i][j];
                if(cell.getType()==CellType.SLOT&& !cell.isOccupied() && isVehicleValid(cell, car, area)){
                    List<Cell> path=parkingService.findPath(area,gate,cell);
                    if(!path.isEmpty()){
                        int dist=path.size();
                        if(dist<minDist){
                            minDist=dist;
                            best=area.getGrid()[i][j];
                        }
                    }

                }
            }
        }
        return best;
    }

    private boolean isVehicleValid(Cell cell, Vehicle car, Parking area){
        if(car.getType()==VehicleType.AMBULANCE  ){
            return cell.getSlotSize() == VehicleSize.LARGE;
        }


        if(car.getType()==VehicleType.DISABLED ){
            return cell.getSlotSize() == VehicleSize.STANDARD
                    && isLiftPresent(cell, area);
        }


        return cell.getSlotSize().getVehicleSize()
                >= car.getSize().getVehicleSize();
    }

    private boolean isLiftPresent(Cell cell, Parking area){
        int r=cell.getRow();
        int c=cell.getCol();

        int radius = 2;

        for(int i = r - radius; i <= r + radius; i++) {
            for(int j = c - radius; j <= c + radius; j++) {
                if(i >= 0 && i < area.getRows()
                        && j >= 0 && j < area.getCols()) {
                    if(area.getGrid()[i][j].getType() == CellType.LIFT) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
