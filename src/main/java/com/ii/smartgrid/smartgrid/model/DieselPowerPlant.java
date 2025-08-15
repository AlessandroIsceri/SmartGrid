package com.ii.smartgrid.smartgrid.model;

public class DieselPowerPlant extends NonRenewablePowerPlant{
    
    private double enginePower;
    private double efficiency;
    
    public DieselPowerPlant(){
        super();
    }


    public double getEnginePower() {
        return enginePower;
    }


    public void setEnginePower(double enginePower) {
        this.enginePower = enginePower;
    }


    public double getEfficiency() {
        return efficiency;
    }


    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }


    @Override
    public double getHourlyProduction(Object... weatherConditions) {
        return enginePower * efficiency;
    }


    @Override
    public String toString() {
        return "DieselPowerPlant [enginePower=" + enginePower + ", efficiency=" + efficiency + "]";
    }

    
}
