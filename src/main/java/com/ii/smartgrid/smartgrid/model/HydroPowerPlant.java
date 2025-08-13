package com.ii.smartgrid.smartgrid.model;

public class HydroPowerPlant extends RenewablePowerPlant{
    
    private double efficiency;
    private double flowRate; // m^3/s
    private double headHeight; // m

    private final double WATER_DENSITY = 1000.0; // kg/m^3
    private final double GRAVITATIONAL_ACCELERATION = 9.81; // m/s^2

    public HydroPowerPlant(){
        super();
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public double getFlowRate() {
        return flowRate;
    }

    public void setFlowRate(double flowRate) {
        this.flowRate = flowRate;
    }



    public double getHeadHeight() {
        return headHeight;
    }



    public void setHeadHeight(double headHeight) {
        this.headHeight = headHeight;
    }



    @Override
    public double getHourlyProduction(Object... weatherConditions) {
		double energyProd = 0.0;
        energyProd = efficiency * WATER_DENSITY * GRAVITATIONAL_ACCELERATION * flowRate * headHeight;
        return energyProd;
	}   


}