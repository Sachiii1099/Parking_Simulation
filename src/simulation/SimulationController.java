package simulation;

import BasicBuildingBlocks.*;
import BasicBuildingBlocks.enums.*;
import AllPerformingFunctions.*;

import java.util.*;

public class SimulationController {

    private final Parking parking;
    private final VehicleGeneration gen;
    private final SlotAllocation alloc;
    private final ParkingService pathService;

    // Active cars: car -> its current path (remaining cells to walk)
    private final Map<Vehicle, List<Cell>> activePaths = new LinkedHashMap<>();

    // Car -> which floor it's on
    private final Map<Vehicle, Integer> carFloor = new HashMap<>();

    // Car -> current cell position
    private final Map<Vehicle, Cell> carPosition = new HashMap<>();

    // Car -> ticks remaining before it leaves
    private final Map<Vehicle, Integer> parkedCars = new HashMap<>();

    // Reservation table: floor -> cell -> set of vehicles that reserved it this tick
    private final Map<Integer, Map<Cell, Vehicle>> reservations = new HashMap<>();

    private int clock = 0;
    private final Random random = new Random();

    public SimulationController(Parking parking) {
        this.parking = parking;
        this.gen = new VehicleGeneration();
        this.alloc = new SlotAllocation();
        this.pathService = new ParkingService();
    }

    public void runTick() {
        clock++;
        clearReservations();

        // Step 1: Move all active cars one cell forward (collision-aware)
        moveAllCars();

        // Step 2: Decrement parked car timers, remove those that leave
        tickParkedCars();

        // Step 3: Spawn new vehicle
        spawnVehicle();
    }

    private void clearReservations() {
        reservations.clear();
        // Reserve current positions of all moving cars
        for (Map.Entry<Vehicle, Cell> e : carPosition.entrySet()) {
            Vehicle car = e.getKey();
            Cell cell = e.getValue();
            int floor = carFloor.get(car);
            reserveCell(floor, cell, car);
        }
    }

    private void reserveCell(int floor, Cell cell, Vehicle car) {
        reservations.computeIfAbsent(floor, k -> new HashMap<>()).put(cell, car);
    }

    private boolean isCellReserved(int floor, Cell cell, Vehicle requester) {
        Map<Cell, Vehicle> floorRes = reservations.get(floor);
        if (floorRes == null) return false;
        Vehicle owner = floorRes.get(cell);
        return owner != null && owner != requester;
    }

    private void moveAllCars() {
        // Sort by priority: higher priority vehicles move first
        List<Vehicle> sortedCars = new ArrayList<>(activePaths.keySet());
        sortedCars.sort((a, b) -> b.getType().getPriority() - a.getType().getPriority());

        for (Vehicle car : sortedCars) {
            List<Cell> path = activePaths.get(car);
            if (path == null || path.isEmpty()) continue;

            int floor = carFloor.get(car);
            Cell nextCell = path.get(0);

            if (isCellReserved(floor, nextCell, car)) {
                // Cell is taken — wait this tick (no movement)
                System.out.println("  Car " + car.getId() + " waiting at " +
                    carPosition.get(car).getRow() + "," + carPosition.get(car).getCol() +
                    " (collision avoided)");
                continue;
            }

            // Move to next cell
            path.remove(0);
            carPosition.put(car, nextCell);
            reserveCell(floor, nextCell, car);

            // If path is empty, car has reached its slot
            if (path.isEmpty()) {
                Cell slot = nextCell;
                slot.parking(car);
                activePaths.remove(car);
                int stayTicks = 20 + random.nextInt(30); // 5-15 ticks
                parkedCars.put(car, stayTicks);
                System.out.println("  Car " + car.getId() + " parked at " +
                    slot.getRow() + "," + slot.getCol() + " for " + stayTicks + " ticks");
            }
        }
    }

    private void tickParkedCars() {
        Iterator<Map.Entry<Vehicle, Integer>> it = parkedCars.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Vehicle, Integer> entry = it.next();
            Vehicle car = entry.getKey();
            int remaining = entry.getValue() - 1;

            if (remaining <= 0) {
                // Car leaves — free its slot
                int floor = carFloor.get(car);
                Cell pos = carPosition.get(car);
                if (pos != null && pos.getType() == CellType.SLOT) {
                    pos.setOccupied(false);
                }
                carPosition.remove(car);
                carFloor.remove(car);
                it.remove();
                System.out.println("  Car " + car.getId() + " left the parking.");
            } else {
                entry.setValue(remaining);
            }
        }
    }

    private void spawnVehicle() {
        Vehicle car = gen.vehicleGenearte(parking.getFloor(0).getGates());
        if (car == null) {
            System.out.println("Tick " + clock + ": No vehicle arrived.");
            return;
        }

        Cell gate = car.getEntryGate();
        Cell slot = alloc.slotAllocate(parking, car, gate);

        if (slot == null) {
            System.out.println("Tick " + clock + ": Car " + car.getId() +
                " (" + car.getType() + ") arrived but no slot available.");
            return;
        }

        // Find which floor the slot is on
        int floorIndex = findFloor(slot);
        if (floorIndex == -1) return;

        // Find path on that floor
        Cell startCell = (floorIndex == 0) ? gate : parking.getFloor(floorIndex).getGates().get(0);
        List<Cell> path = pathService.findPath(parking.getFloor(floorIndex), startCell, slot);

        if (path.isEmpty()) {
            System.out.println("Tick " + clock + ": Car " + car.getId() + " no path found.");
            return;
        }

        // Remove start cell from path (car begins there)
        path.remove(0);

        activePaths.put(car, path);
        carFloor.put(car, floorIndex);
        carPosition.put(car, startCell);

        System.out.println("Tick " + clock + ": Car " + car.getId() +
            " (" + car.getType() + ", " + car.getSize() + ")" +
            " entering from gate " + gate.getRow() + "," + gate.getCol() +
            " heading to slot " + slot.getRow() + "," + slot.getCol() +
            " on floor " + floorIndex);
    }

    private int findFloor(Cell slot) {
        for (int f = 0; f < parking.getTotalFloors(); f++) {
            Cell[][] grid = parking.getFloor(f).getGrid();
            for (int i = 0; i < parking.getFloor(f).getRows(); i++)
                for (int j = 0; j < parking.getFloor(f).getCols(); j++)
                    if (grid[i][j] == slot) return f;
        }
        return -1;
    }

    public int getClock() { return clock; }
    public Parking getParking() { return parking; }
    public Map<Vehicle, Cell> getCarPositions() { return carPosition; }
    public Map<Vehicle, Integer> getCarFloors() { return carFloor; }
    public Map<Vehicle, Integer> getParkedCars() { return parkedCars; }
}