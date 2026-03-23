package simulation;

import model.Parking;
import model.Cell;
import model.Vehicle;
import model.enums.CellType;
import model.enums.VehicleSize;
import service.VehicleGeneration;
import service.SlotAllocation;
import service.ParkingService;
import java.util.*;


public class WorkingSimulation {
    private Parking area;
    private VehicleGeneration gen;
    private SlotAllocation alloc;
    private ParkingService path;
    private int clock;

    public WorkingSimulation(Parking area){
        this.area=area;
        this.gen=new VehicleGeneration();
        this.alloc=new SlotAllocation();
        this.path=new ParkingService();
        this.clock=0;
    }

    public void runClock(){
        clock++;
        Vehicle car= gen.vehicleGenearte(area.getGates());
        if(car==null){
            System.out.println("At time "+ clock+" no new vehicle arrived");
            printParking(area);
            return;
        }

        Cell gate =car.getEntryGate();
        Cell slot=alloc.slotAllocate(area,car,gate);
        if(slot==null){
            System.out.println("At time "+clock+" no possible and suitable slot available for the vehicle of type"+ car.getType()+" of size "+ car.getSize());

        }else {
            slot.setOccupied(true);
            List<Cell> pathList= path.findPath(area, gate,slot);
            System.out.println("At time "+clock+" A vechicleId "+ car.getId()+" of type "+car.getType()+ " of size " + car.getSize() +" from gate position "+gate.getRow()+","+gate.getCol()+" is parked at "+slot.getRow()+","+slot.getCol()+"whose distance is" + pathList.size());
        }

        printParking(area);

    }
    public void printParking(Parking area){
        for(int i=0;i<area.getRows();i++){
            for(int j=0;j< area.getCols();j++){
                Cell cell=area.getGrid()[i][j];
                if(cell.isOccupied()){
                    System.out.print(" @ ");
                }else if(cell.getType()==CellType.SLOT){
                    if(cell.getSlotSize()==VehicleSize.LARGE){
                        System.out.print(" LS ");
                    }else{
                        System.out.print(" SS ");
                    }
                }
                else if(cell.getType()== CellType.ROAD){
                    System.out.print(" R ");
                }else if(cell.getType()==CellType.GATE){
                    System.out.print(" G ");
                }else if(cell.getType()==CellType.LIFT){
                    System.out.print(" L ");
                }else if(cell.getType()==CellType.BLOCK){
                    System.out.print(" # ");
                }
            }
            System.out.println();
        }
    }
}
