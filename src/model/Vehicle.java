package model;

import model.enums.VehicleType;
import model.enums.VehicleSize;

public class Vehicle {
    private int id;
    private VehicleType type;
    private Cell entryGate;
    private VehicleSize size;
    public Vehicle(int id,VehicleType type,Cell entryGate,VehicleSize size){
        this.id=id;
        this.type=type;
        this.entryGate = entryGate;
        this.size=type.resolveSize(size);
    }
    public int getId(){return id;}
    public VehicleSize getSize(){ return size;}
    public VehicleType getType(){return type;}
    public Cell getEntryGate(){return entryGate;}


}
