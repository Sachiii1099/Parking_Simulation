package model.enums;

public enum VehicleSize {
    STANDARD(1),
    LARGE(2);

    private final int value;
    VehicleSize(int value){
        this.value=value;
    }
    public int getVehicleSize(){
        return value;
    }

}
