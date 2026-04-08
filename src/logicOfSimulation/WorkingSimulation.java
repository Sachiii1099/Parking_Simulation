package simulation;

import model.Parking;
import model.Cell;
import model.ParkingFloor;
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

    public WorkingSimulation(Parking area) {
        this.area = area;
        this.gen = new VehicleGeneration();
        this.alloc = new SlotAllocation();
        this.path = new ParkingService();
        this.clock = 0;
    }

    public void runClock() {
        clock++;
        Vehicle car = gen.vehicleGenearte(area.getFloor(0).getGates());
        if (car == null) {
            System.out.println("At time " + clock + " no new vehicle arrived");
            printParking(area);
            return;
        }

        Cell gate = car.getEntryGate();
        Cell slot = alloc.slotAllocate(area, car, gate);
        if (slot == null) {
            System.out.println("At time " + clock + " no possible and suitable slot available for the vehicle of type" + car.getType() + " of size " + car.getSize());

        } else {
            int floorIndex = -1;
            for (int f = 0; f < area.getTotalFloors(); f++) {
                if (area.getFloor(f).getGrid()[slot.getRow()][slot.getCol()] == slot) {
                    floorIndex = f;
                    break;
                }
            }
            slot.parking(car);
            Cell actualGate = gate; // Default to the basement gate
            if (floorIndex > 0) {
                int shortestDist = Integer.MAX_VALUE;
                // Check which gate on this upper floor is closest to the slot
                for (Cell g : area.getFloor(floorIndex).getGates()) {
                    List<Cell> p = path.findPath(area.getFloor(floorIndex), g, slot);
                    if (!p.isEmpty() && p.size() < shortestDist) {
                        shortestDist = p.size();
                        actualGate = g; // Update to the correct lift door!
                    }
                }
            }
            System.out.println("At time   " + clock + " A vec  hicleId " + car.getId() +
                    " oftype " + car.getType() +
                    " ofSize " + car.getSize() +
                    " fromGate postion " + actualGate.getRow() + "," + actualGate.getCol() +
                    " ised on Floor " + floorIndex +
                    " atposition " + slot.getRow() + "," + slot.getCol());

        }

        printParking(area);

    }

    public void printParking(Parking building) {
        for (int f = 0; f < building.getTotalFloors(); f++) {
            ParkingFloor currentFloor = building.getFloor(f);
            System.out.println("\nThe floor   " + f + " -");
            for (int i = 0; i < currentFloor.getRows(); i++) {
                for (int j = 0; j < currentFloor.getCols(); j++) {
                    Cell cell = currentFloor.getGrid()[i][j];
                    if (cell.isOccupied()) {
                        switch (cell.getparkedVehicle()) {
                            case NORMAL:
                                System.out.print(" 🚗 ");
                                break;
                            case VIP:
                                System.out.print(" 🚙 ");
                                break;
                            case DISABLED:
                                System.out.print(" 🦽 ");
                                break;
                            case AMBULANCE:
                                System.out.print(" 🚑 ");
                                break;

                        }
                    } else if (cell.getType() == CellType.SLOT) {
                        if (cell.getSlotSize() == VehicleSize.LARGE) {
                            System.out.print(" 2️⃣ ");
                        } else {
                            System.out.print(" 1️⃣ ");
                        }
                    } else if (cell.getType() == CellType.ROAD) {
                        System.out.print(" \uD83C\uDFC1️ ");
                    } else if (cell.getType() == CellType.GATE) {
                        System.out.print(" 🧲 ");
                    } else if (cell.getType() == CellType.LIFT) {
                        System.out.print(" 🛗 ");
                    } else if (cell.getType() == CellType.BLOCK) {
                        System.out.print(" ❎ ");
                    }
                }
                System.out.println();
            }
        }
    }
}
