package com.ii.smartgrid.model;


public class NonRenewablePowerPlantInfo {
    private String name;
    private double maxTurnProduction;
    private boolean on;

    public NonRenewablePowerPlantInfo() {
    }

    public NonRenewablePowerPlantInfo(String name, double maxTurnProduction, boolean on) {
        this.name = name;
        this.maxTurnProduction = maxTurnProduction;
        this.on = on;
    }

    public double getMaxTurnProduction() {
        return maxTurnProduction;
    }

    public void setMaxTurnProduction(double maxTurnProduction) {
        this.maxTurnProduction = maxTurnProduction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }


}