package com.ii.smartgrid.smartgrid.model.entities;

public abstract class NonRenewablePowerPlant extends PowerPlant {

    protected String loadManagerName;

    protected NonRenewablePowerPlant() {
        super();
    }

    public abstract double getHourlyProduction(Object... weatherConditions);

    public String getLoadManagerName() {
        return loadManagerName;
    }

    public void setLoadManagerName(String loadManagerName) {
        this.loadManagerName = loadManagerName;
    }

}
