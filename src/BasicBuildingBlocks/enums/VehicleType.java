package model.enums;

import model.enums.VehicleSize;


public enum VehicleType {
                NORMAL(2,false,null),
    VIP(6,false,null),

     DISABLED(8,true,VehicleSize.STANDARD),
    AMBULANCE(10,true,VehicleSize.LARGE);

    private final int priority;


            private final boolean isFixedSize;
            private final VehicleSize fixedSize;

    VehicleType(int priority,boolean isFixedSize,VehicleSize fixedSize){
        this.priority= priority;
                this.isFixedSize=isFixedSize;
        this.fixedSize=fixedSize;
    }

    public int getPriority(){return priority;}


    public boolean isFixedSize(){return isFixedSize;}
      public VehicleSize getFixedSize(){return fixedSize;}

    public VehicleSize resolveSize(VehicleSize generatedSize){

        if(isFixedSize){return fixedSize;}
         else return generatedSize;
    }

}
