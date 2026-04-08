package BasicBuildingBlocks.enums;

public enum VehicleSize {
    STANDARD(6),
    LARGE(7);

    private final int value;
    VehicleSize(int value){
        this.value=value;
    }
    public int getVehicleSize(){
        return value;
    }

}
