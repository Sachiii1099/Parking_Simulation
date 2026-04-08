package BasicBuildingBlocks;

import java.util.List;

public class Parking {
    private  List<ParkingFloor> floors;

    public Parking(List<ParkingFloor> floors) {
        this.floors = floors;
    }

    public List<ParkingFloor> getFloors() {
        return floors;
    }

    public ParkingFloor getFloor(int index) {
        if (index >= 0 && index < floors.size()) {
            return floors.get(index);
        }
        return null;
    }

    public int getTotalFloors() {
        return floors.size();
    }
}