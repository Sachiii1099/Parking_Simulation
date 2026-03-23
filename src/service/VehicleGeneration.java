package service;
import model.Vehicle;
import model.Cell;
import model.enums.VehicleType;
import model.enums.VehicleSize;

import java.util.List;
import java.util.Random;


public class VehiceGeneration {
    private Random random =new Random();
    private int vehicleCounter =0;
    private double generationProbaility=0.7;
    private double normalProbability=0.6;
    private double ambulanceProbability=0.1;
    private double disabledProbability=0.2;
    private double vipProbability = 0.1;

    private VehicleType getVehicleType(){
        double r = random.nextDouble();

        if(r<normalProbability) return VehicleType.NORMAL;
        else if(r<normalProbability+ambulanceProbability) return VehicleType.AMBULANCE;
        else if(r<normalProbability+ambulanceProbability+ disabledProbability) return VehicleType.DISABLED;
        else return VehicleType.VIP;
    }

    public Vehicle vehicleGenearte(List<Cell>gates){
        if(random.nextDouble() > generationProbaility) {
            return null;  // no vehicle this tick
        }
        VehicleType type = getVehicleType();
        VehicleSize rolledSize = random.nextDouble() < 0.6 ?
                VehicleSize.STANDARD : VehicleSize.LARGE;
        VehicleSize actualSize = type.resolveSize(rolledSize);
        Cell gate = gates.get(random.nextInt(gates.size()));
        Vehicle vehicle = new Vehicle(vehicleCounter, type, gate, actualSize);
        vehicleCounter++;
        return vehicle;
    }

}
