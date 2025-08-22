package com.ii.smartgrid.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DieselPowerPlant extends NonRenewablePowerPlant {

    private double enginePower;
    private double efficiency;

    public DieselPowerPlant() {
        super();
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public double getEnginePower() {
        return enginePower;
    }

    public void setEnginePower(double enginePower) {
        this.enginePower = enginePower;
    }

    @Override
    public double getHourlyProduction(Object... parameters) {
        double turnRequest = (double) parameters[0];
        double powerUsed = Math.min(enginePower, turnRequest);
        return powerUsed * efficiency;
    }

    @JsonIgnore
    @Override
    public double getMaxHourlyProduction() {
        return enginePower * efficiency;
    }

    @Override
    public String toString() {
        return "DieselPowerPlant [on=" + on + ", enginePower=" + enginePower + ", efficiency=" + efficiency
                + ", turnRequest=" + turnRequest + "]";
    }



}
