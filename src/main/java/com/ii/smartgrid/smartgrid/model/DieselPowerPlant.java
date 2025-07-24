package com.ii.smartgrid.smartgrid.model;

import java.util.Map;

import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

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
        double energyProd = 0.0;
        energyProd = enginePower * efficiency;
        return energyProd;
    }
    
}
