package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;

public abstract class NonRenewablePowerPlant extends PowerPlant{

    // protected double hourlyProduction;
    // protected double requestedEnergy;
    protected String loadManagerName;

    public NonRenewablePowerPlant() {
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
