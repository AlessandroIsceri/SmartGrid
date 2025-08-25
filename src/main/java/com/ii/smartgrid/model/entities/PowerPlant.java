package com.ii.smartgrid.model.entities;

public abstract class PowerPlant extends CustomObject {

    protected String gridName;

    public String getGridName() {
        return gridName;
    }

    public void setGridName(String gridName) {
        this.gridName = gridName;
    }

    public abstract double getHourlyProduction(Object... parameters);

}

