package AllPerformingFunctions;
import BasicBuildingBlocks.Vehicle;
import BasicBuildingBlocks.Cell;
import BasicBuildingBlocks.enums.VehicleType;
import BasicBuildingBlocks.enums.VehicleSize;

import java.util.List;
import java.util.Random;


public class VehicleGeneration {


    private Random random =new Random();


    private int vehicleCounter =0;
        private double gProbaility=0.7;



          private double nProbability =0.6;



        private double aProbability =0.1;
    private double dProbability =0.2;
                 private double vProbability = 0.1;

    private VehicleType getVehicleType(){

        double r = random.nextDouble();

        if(r< nProbability) return VehicleType.NORMAL;




                else if(r< nProbability + aProbability) return VehicleType.AMBULANCE;

                      else if(r< nProbability + aProbability + dProbability) return VehicleType.DISABLED;
        else return VehicleType.VIP;
    }

    public Vehicle vehicleGenearte(List<Cell>gates){


        if(random.nextDouble() > gProbaility) {
            return null;  // no vehicle this tick
        }
                     VehicleType type = getVehicleType();

        VehicleSize rolledSize = random.nextDouble() < 0.6 ? VehicleSize.STANDARD : VehicleSize.LARGE;





                                         VehicleSize actualSize = type.resolveSize(rolledSize);
                      Cell gate = gates.get(random.nextInt(gates.size()));
                 Vehicle vehicle = new Vehicle(vehicleCounter, type, gate, actualSize);



            vehicleCounter++;
        return vehicle;
    }

}
