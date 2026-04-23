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

    private final Map<Vehicle, List<Cell>> activePaths = new LinkedHashMap<>();
    private final Map<Vehicle, Integer> carFloor = new HashMap<>();
    private final Map<Vehicle, Cell> carPosition = new HashMap<>();
    private final Map<Vehicle, Integer> parkedCars = new HashMap<>();
    private final Map<Vehicle, Integer> waitTicks = new HashMap<>();

    // floor -> cell -> vehicle currently occupying it (moving cars only)
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
        moveAllCars();
        tickParkedCars();
        spawnVehicle();
    }

    // ─── Reservations ────────────────────────────────────────────────

    private void clearReservations() {
        reservations.clear();
        // Pre-reserve current positions of all moving cars
        for (Map.Entry<Vehicle, Cell> e : carPosition.entrySet()) {
            Vehicle car = e.getKey();
            if (activePaths.containsKey(car)) { // only moving cars
                reserveCell(carFloor.get(car), e.getValue(), car);
            }
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

    // ─── Movement ────────────────────────────────────────────────────

    private void moveAllCars() {
        List<Vehicle> sortedCars = new ArrayList<>(activePaths.keySet());
        // Higher priority moves first — ambulance > disabled > vip > normal
        sortedCars.sort((a, b) -> b.getType().getPriority() - a.getType().getPriority());

        for (Vehicle car : sortedCars) {
            List<Cell> path = activePaths.get(car);
            if (path == null || path.isEmpty()) continue;

            int floor = carFloor.get(car);
            Cell nextCell = path.get(0);

            if (isCellReserved(floor, nextCell, car)) {
                int waited = waitTicks.getOrDefault(car, 0) + 1;
                waitTicks.put(car, waited);
                System.out.println("  Car " + car.getId() + " blocked, waited " + waited + " ticks");

                // After 3 ticks waiting, try to replan around blocker
                if (waited >= 3) {
                    waitTicks.put(car, 0);
                    boolean replanned = replanAround(car, floor, nextCell);
                    if (!replanned) {
                        // Completely stuck — remove car to prevent permanent jam
                        System.out.println("  Car " + car.getId() + " unresolvable jam — removed.");
                        activePaths.remove(car);
                        carPosition.remove(car);
                        carFloor.remove(car);
                        waitTicks.remove(car);
                    }
                }
                continue;
            }

            // Successful move
            waitTicks.put(car, 0);
            path.remove(0);
            carPosition.put(car, nextCell);
            reserveCell(floor, nextCell, car);

            // Reached destination slot
            if (path.isEmpty()) {
                nextCell.parking(car);
                activePaths.remove(car);
                waitTicks.remove(car);
                int stay = 20 + random.nextInt(30);
                parkedCars.put(car, stay);
                System.out.println("  Car " + car.getId() + " parked at ["
                        + nextCell.getRow() + "," + nextCell.getCol()
                        + "] floor " + floor + " for " + stay + " ticks");
            }
        }
    }

    /**
     * Try to find an alternative path that avoids the currently blocked cell.
     * We temporarily mark the blocker's cell as a block, replan, then restore.
     */
    private boolean replanAround(Vehicle car, int floor, Cell blockedCell) {
        Cell current = carPosition.get(car);
        List<Cell> oldPath = activePaths.get(car);
        if (oldPath == null || oldPath.isEmpty()) return false;
        Cell destination = oldPath.get(oldPath.size() - 1);

        // Temporarily mark blocked cell as BLOCK so BFS avoids it
        CellType original = blockedCell.getType();
        blockedCell.setType(CellType.BLOCK);

        List<Cell> newPath = pathService.findPath(parking.getFloor(floor), current, destination);

        // Restore original type
        blockedCell.setType(original);

        if (newPath.size() > 1) {
            newPath.remove(0); // remove current position
            activePaths.put(car, newPath);
            System.out.println("  Car " + car.getId() + " replanned, new path length: " + newPath.size());
            return true;
        }
        return false;
    }

    // ─── Parked Cars ─────────────────────────────────────────────────

    private void tickParkedCars() {
        Iterator<Map.Entry<Vehicle, Integer>> it = parkedCars.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Vehicle, Integer> entry = it.next();
            Vehicle car = entry.getKey();
            int remaining = entry.getValue() - 1;

            if (remaining <= 0) {
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

    // ─── Spawn ───────────────────────────────────────────────────────

    private void spawnVehicle() {
        Vehicle car = gen.vehicleGenearte(parking.getFloor(0).getGates());
        if (car == null) {
            System.out.println("Tick " + clock + ": No vehicle arrived.");
            return;
        }

        Cell gate = car.getEntryGate();
        Cell slot = alloc.slotAllocate(parking, car, gate);

        if (slot == null) {
            System.out.println("Tick " + clock + ": Car " + car.getId()
                    + " (" + car.getType() + ") — no slot available.");
            return;
        }

        // Find which floor the slot is on by position match (not reference)
        int targetFloor = findFloorByPosition(slot);
        if (targetFloor == -1) {
            System.out.println("Tick " + clock + ": Car " + car.getId() + " — floor not found.");
            return;
        }

        // Get the actual cell reference on that floor (fixes copied floor bug)
        Cell actualSlot = parking.getFloor(targetFloor)
                .getGrid()[slot.getRow()][slot.getCol()];

        // Get start cell: floor 0 uses entry gate, upper floors use nearest gate on that floor
        Cell startCell;
        if (targetFloor == 0) {
            startCell = gate;
        } else {
            startCell = getNearestGate(parking.getFloor(targetFloor), actualSlot);
            if (startCell == null) {
                System.out.println("Tick " + clock + ": Car " + car.getId()
                        + " — no gate on floor " + targetFloor);
                return;
            }
        }

        List<Cell> path = pathService.findPath(
                parking.getFloor(targetFloor), startCell, actualSlot);

        if (path.isEmpty()) {
            System.out.println("Tick " + clock + ": Car " + car.getId()
                    + " — no path on floor " + targetFloor);
            return;
        }

        path.remove(0); // car starts at startCell, don't re-traverse it

        activePaths.put(car, path);
        carFloor.put(car, targetFloor);
        carPosition.put(car, startCell);

        System.out.println("Tick " + clock + ": Car " + car.getId()
                + " (" + car.getType() + ", " + car.getSize() + ")"
                + " floor " + targetFloor
                + " gate [" + startCell.getRow() + "," + startCell.getCol() + "]"
                + " → slot [" + actualSlot.getRow() + "," + actualSlot.getCol() + "]");
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    /**
     * Find floor by matching row/col position instead of object reference.
     * Fixes the bug where copied floors never matched by == reference.
     */
    private int findFloorByPosition(Cell slot) {
        for (int f = 0; f < parking.getTotalFloors(); f++) {
            ParkingFloor floor = parking.getFloor(f);
            Cell candidate = floor.getGrid()[slot.getRow()][slot.getCol()];
            if (candidate.getType() == CellType.SLOT
                    && candidate.getSlotSize() == slot.getSlotSize()
                    && !candidate.isOccupied()) {
                return f;
            }
        }
        return -1;
    }

    /**
     * Get the gate on a given floor that is nearest (Manhattan distance) to the target slot.
     */
    private Cell getNearestGate(ParkingFloor floor, Cell target) {
        Cell best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Cell g : floor.getGates()) {
            int dist = Math.abs(g.getRow() - target.getRow())
                    + Math.abs(g.getCol() - target.getCol());
            if (dist < bestDist) {
                bestDist = dist;
                best = g;
            }
        }
        return best;
    }

    // ─── Getters ─────────────────────────────────────────────────────

    public int getClock() { return clock; }
    public Parking getParking() { return parking; }
    public Map<Vehicle, Cell> getCarPositions() { return carPosition; }
    public Map<Vehicle, Integer> getCarFloors() { return carFloor; }
    public Map<Vehicle, Integer> getParkedCars() { return parkedCars; }
}