package simulation;

import BasicBuildingBlocks.*;
import BasicBuildingBlocks.enums.*;
import AllPerformingFunctions.*;

import java.util.*;

public class SimulationController {

    private final Parking parking;
    private final VehicleGeneration gen;
    private final SlotAllocation alloc;


    private final Map<Vehicle, List<Cell>> activePaths = new LinkedHashMap<>();


    private final Map<Vehicle, Integer> carFloor = new HashMap<>();


    private final Map<Vehicle, Cell> carPosition = new HashMap<>();


    private final Map<Vehicle, Integer> parkedCars = new HashMap<>();


    private final Set<Cell> reservedSlots = new HashSet<>();


    private final Map<Vehicle, Integer> waitTicks = new HashMap<>();

    private int clock = 0;
    private final Random random = new Random();

    public SimulationController(Parking parking) {
        this.parking = parking;
        this.gen     = new VehicleGeneration();
        this.alloc   = new SlotAllocation();
    }

    // ═══════════════════════════════════════════════
    //  Main tick
    // ═══════════════════════════════════════════════

    public void runTick() {
        clock++;
        moveAllCars();
        tickParkedCars();
        spawnVehicle();
    }



    private void moveAllCars() {
        // Higher priority vehicles move first
        List<Vehicle> order = new ArrayList<>(activePaths.keySet());
        order.sort((a, b) -> b.getType().getPriority() - a.getType().getPriority());

        for (Vehicle car : order) {
            List<Cell> path = activePaths.get(car);
            if (path == null || path.isEmpty()) continue;

            int  floor    = carFloor.get(car);
            Cell nextCell = getActualCell(floor, path.get(0));

            // === Collision check: is next cell taken by another car? ===
            if (nextCell.hasAnyCar()) {
                int waited = waitTicks.getOrDefault(car, 0) + 1;
                waitTicks.put(car, waited);
                System.out.println("  Car " + car.getId()
                        + " waiting " + waited + " tick(s) — next cell ["
                        + nextCell.getRow() + "," + nextCell.getCol() + "] occupied");

                if (waited >= 3) {
                    // Try to replan around the blocker
                    waitTicks.put(car, 0);
                    Cell dest = getActualCell(floor, path.get(path.size() - 1));
                    Cell curr = getActualCell(floor, carPosition.get(car));
                    List<Cell> newPath = bfs(floor, curr, dest);
                    if (newPath.size() > 1) {
                        newPath.remove(0); // drop current position
                        activePaths.put(car, newPath);
                        System.out.println("  Car " + car.getId() + " replanned.");
                    } else {
                        System.out.println("  Car " + car.getId() + " stuck — removed.");
                        forceRemoveCar(car);
                    }
                }
                continue;
            }

            // === Move car from current cell to nextCell ===
            Cell currCell = getActualCell(floor, carPosition.get(car));

            // Leave current cell (only clear if it's not a parked slot)
            if (!currCell.isOccupied()) {
                currCell.leave();
            }

            // Enter next cell
            nextCell.enter(car);
            path.remove(0);
            carPosition.put(car, nextCell);
            waitTicks.put(car, 0);

            // === Arrived at destination slot? ===
            if (path.isEmpty()) {
                nextCell.park(car);          // mark as parked
                activePaths.remove(car);
                waitTicks.remove(car);
                int stay = 20 + random.nextInt(30);
                parkedCars.put(car, stay);
                System.out.println("  Car " + car.getId()
                        + " parked at [" + nextCell.getRow() + ","
                        + nextCell.getCol() + "] floor " + floor
                        + " staying " + stay + " ticks");
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  Parked car timers
    // ═══════════════════════════════════════════════

    private void tickParkedCars() {
        Iterator<Map.Entry<Vehicle, Integer>> it = parkedCars.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Vehicle, Integer> entry = it.next();
            Vehicle car       = entry.getKey();
            int     remaining = entry.getValue() - 1;

            if (remaining <= 0) {
                Cell pos = carPosition.get(car);
                if (pos != null) {
                    pos.unpark();              // free the slot on the grid
                    reservedSlots.remove(pos);
                }
                carPosition.remove(car);
                carFloor.remove(car);
                it.remove();
                System.out.println("  Car " + car.getId() + " left.");
            } else {
                entry.setValue(remaining);
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  Spawn
    // ═══════════════════════════════════════════════

    private void spawnVehicle() {
        Vehicle car = gen.vehicleGenearte(parking.getFloor(0).getGates());
        if (car == null) {
            System.out.println("Tick " + clock + ": No vehicle arrived.");
            return;
        }

        Cell gate = getActualCell(0, car.getEntryGate());

        // Gate must be completely free
        if (gate.hasAnyCar()) {
            System.out.println("Tick " + clock + ": Car " + car.getId()
                    + " — gate busy, turned away.");
            return;
        }

        // Allocate slot (hide reserved slots from allocator)
        Cell slot = allocateExcludingReserved(car, gate);
        if (slot == null) {
            System.out.println("Tick " + clock + ": Car " + car.getId()
                    + " — no slot available.");
            return;
        }

        int targetFloor = findFloorOf(slot);
        if (targetFloor == -1) {
            System.out.println("Tick " + clock + ": Car " + car.getId()
                    + " — floor not found.");
            return;
        }

        Cell actualSlot  = getActualCell(targetFloor, slot);
        Cell startCell   = (targetFloor == 0)
                ? gate
                : getNearestGate(parking.getFloor(targetFloor), actualSlot);

        if (startCell == null) {
            System.out.println("Tick " + clock + ": Car " + car.getId()
                    + " — no gate on floor " + targetFloor);
            return;
        }

        // BFS on grid — sees all cars because occupant is set on cells
        List<Cell> path = bfs(targetFloor, startCell, actualSlot);
        if (path.isEmpty()) {
            System.out.println("Tick " + clock + ": Car " + car.getId()
                    + " — no path found.");
            return;
        }

        // Claim slot and place car on start cell
        reservedSlots.add(actualSlot);
        path.remove(0); // car is already at startCell

        startCell.enter(car);
        activePaths.put(car, path);
        carFloor.put(car, targetFloor);
        carPosition.put(car, startCell);

        System.out.println("Tick " + clock + ": Car " + car.getId()
                + " (" + car.getType() + ", " + car.getSize() + ")"
                + " floor=" + targetFloor
                + " gate=[" + startCell.getRow() + "," + startCell.getCol() + "]"
                + " slot=[" + actualSlot.getRow() + "," + actualSlot.getCol() + "]");
    }

    // ═══════════════════════════════════════════════
    //  BFS — reads directly from grid
    // ═══════════════════════════════════════════════

    /**
     * BFS on the actual grid cells of a floor.
     * A cell is passable if:
     *   - Not a BLOCK
     *   - Not occupied by any car (hasAnyCar() == false)
     *     UNLESS it is the destination
     */
    private List<Cell> bfs(int floorIndex, Cell start, Cell end) {
        ParkingFloor floor = parking.getFloor(floorIndex);
        int rows = floor.getRows(), cols = floor.getCols();

        // Use actual grid cell references
        Cell realStart = getActualCell(floorIndex, start);
        Cell realEnd   = getActualCell(floorIndex, end);

        boolean[][]     visited = new boolean[rows][cols];
        Map<Cell, Cell> parent  = new HashMap<>();
        Queue<Cell>     queue   = new LinkedList<>();

        queue.add(realStart);
        visited[realStart.getRow()][realStart.getCol()] = true;

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            Cell curr = queue.poll();
            if (curr == realEnd) {
                // Reconstruct
                List<Cell> path = new ArrayList<>();
                for (Cell c = realEnd; c != null; c = parent.get(c)) path.add(c);
                Collections.reverse(path);
                return path;
            }

            for (int d = 0; d < 4; d++) {
                int nr = curr.getRow() + dr[d];
                int nc = curr.getCol() + dc[d];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;

                Cell next = floor.getGrid()[nr][nc];
                if (visited[nr][nc])                  continue;
                if (next.getType() == CellType.BLOCK) continue;

                // KEY RULE: any cell with a car on it is a wall,
                // EXCEPT the destination (we are allowed to move into it)
                if (next.hasAnyCar() && next != realEnd) continue;

                visited[nr][nc] = true;
                parent.put(next, curr);
                queue.add(next);
            }
        }
        return new ArrayList<>(); // no path
    }

    // ═══════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════

    /**
     * Always get the true Cell object from the grid by row/col.
     * This fixes the copied-floor reference bug completely.
     */
    private Cell getActualCell(int floor, Cell ref) {
        return parking.getFloor(floor).getGrid()[ref.getRow()][ref.getCol()];
    }

    private Cell allocateExcludingReserved(Vehicle car, Cell gate) {
        // Temporarily mark reserved slots as occupied so allocator skips them
        List<Cell> marked = new ArrayList<>();
        for (Cell s : reservedSlots) {
            if (!s.isOccupied()) {
                s.enter(new Vehicle(-1, VehicleType.NORMAL, gate, VehicleSize.STANDARD));
                marked.add(s);
            }
        }
        Cell result = alloc.slotAllocate(parking, car, gate);
        // Restore
        for (Cell s : marked) s.leave();
        return result;
    }

    private int findFloorOf(Cell slot) {
        for (int f = 0; f < parking.getTotalFloors(); f++) {
            ParkingFloor fl = parking.getFloor(f);
            if (slot.getRow() >= fl.getRows()
                    || slot.getCol() >= fl.getCols()) continue;
            Cell c = fl.getGrid()[slot.getRow()][slot.getCol()];
            // Exact reference match
            if (c == slot) return f;
            // Position+type match for copied floors
            if (c.getType() == CellType.SLOT
                    && c.getSlotSize() == slot.getSlotSize()
                    && !c.isOccupied()
                    && !reservedSlots.contains(c)) return f;
        }
        return -1;
    }

    private Cell getNearestGate(ParkingFloor floor, Cell target) {
        Cell best  = null;
        int  bestD = Integer.MAX_VALUE;
        for (Cell g : floor.getGates()) {
            int d = Math.abs(g.getRow() - target.getRow())
                    + Math.abs(g.getCol() - target.getCol());
            if (d < bestD) { bestD = d; best = g; }
        }
        return best;
    }

    private void forceRemoveCar(Vehicle car) {
        Cell pos = carPosition.get(car);
        if (pos != null && !pos.isOccupied()) pos.leave();
        reservedSlots.remove(pos);
        activePaths.remove(car);
        carPosition.remove(car);
        carFloor.remove(car);
        waitTicks.remove(car);
        parkedCars.remove(car);
    }

    // ═══════════════════════════════════════════════
    //  Getters for renderer
    // ═══════════════════════════════════════════════

    public int getClock()                        { return clock; }
    public Parking getParking()                  { return parking; }
    public Map<Vehicle, Cell> getCarPositions()  { return carPosition; }
    public Map<Vehicle, Integer> getCarFloors()  { return carFloor; }
    public Map<Vehicle, Integer> getParkedCars() { return parkedCars; }
}