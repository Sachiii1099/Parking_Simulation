package AllPerformingFunctions;

import BasicBuildingBlocks.Cell;
import BasicBuildingBlocks.Parking;



import BasicBuildingBlocks.ParkingFloor;
    import BasicBuildingBlocks.Vehicle;
import BasicBuildingBlocks.enums.VehicleSize;
            import BasicBuildingBlocks.enums.CellType;
            import BasicBuildingBlocks.enums.VehicleType;
import java.util.*;


public class SlotAllocation {
    private ParkingService parkingService= new ParkingService();

    public Cell slotAllocate(Parking building, Vehicle car, Cell gate) {

        for (int f = 0; f < building.getTotalFloors(); f++) {
            ParkingFloor currentFloor = building.getFloor(f);
                             Cell best = null;
            int minDist = Integer.MAX_VALUE;

            System.out.println("VehicleID of       " + car.getId() + " " + car.getType() +" of size "+car.getSize()+ " is check ing Floor " + f + "...");




            for (int i = 0; i < currentFloor.getRows(); i++) {
                        for (int j = 0; j < currentFloor.getCols(); j++) {
                            Cell cell = currentFloor.getGrid()[i][j];




                    if (cell.getType() == CellType.SLOT && !cell.isOccupied() && isVehicleValid(cell, car, currentFloor)) {

                        List<Cell> gatesToCheck = new ArrayList<>();


                        if (f == 0) {
                            // Floor 0: Must enter through the specific gate the car arrived at
                                gatesToCheck.add(gate);
                        } else
                        {



                            // Upper floors: Car can arrive via ANY lift/gate on this floor
                            gatesToCheck.addAll(currentFloor.getGates());
                        }


                        for (Cell startCell : gatesToCheck) {
                            List<Cell> path = parkingService.findPath(currentFloor, startCell, cell);

                            if (!path.isEmpty()) {
                                             int dist = path.size();
                                if (dist < minDist) {
                                    minDist = dist;


                                    best = cell;
                                }


                            }


                        }

                    }




               }
            }

            if (best != null) {
                System.out.println("Slot is on Floor " + f);
                return best;
            }

            System.out.println("Floor " + f + " is full, moving to next floor...");
        }

        return null; // All floors checked, no slot found
    }

    private boolean isVehicleValid(Cell cell, Vehicle car, ParkingFloor area){

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

    private boolean isLiftPresent(Cell cell, ParkingFloor area){
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
