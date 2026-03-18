package model;

import model.enums.VehicleType;

public class Vehicle {
    private int id;
    private VehicleType type;
    private Cell entryGate;
    public Vehicle(int id,VehicleType type,Cell entry){
        this.id=id;
        this.type=type;
        this.entryGate = entryGate;
    }
    public int getId(){return id;}
    public VehicleType getType(){return type;}
    public Cell getEntryGate(){return entryGate;}


}
