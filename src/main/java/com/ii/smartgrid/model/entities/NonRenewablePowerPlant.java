package com.ii.smartgrid.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class NonRenewablePowerPlant extends PowerPlant {

    protected String loadManagerName;

    @JsonIgnore
    protected double turnRequest;

    protected NonRenewablePowerPlant() {
        super();
    }

    public abstract double getHourlyProduction(Object... parameters);

    public String getLoadManagerName() {
        return loadManagerName;
    }

    public void setLoadManagerName(String loadManagerName) {
        this.loadManagerName = loadManagerName;
    }

    @JsonIgnore
    public void setTurnRequest(double turnRequest) {
        this.turnRequest = turnRequest;
    }

    @JsonIgnore
    public double getTurnRequest() {
        return turnRequest;
    }

    @JsonIgnore
    public abstract double getMaxHourlyProduction();

}
