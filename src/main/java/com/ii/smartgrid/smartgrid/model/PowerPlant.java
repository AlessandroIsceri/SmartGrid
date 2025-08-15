package com.ii.smartgrid.smartgrid.model;

public abstract class PowerPlant extends CustomObject{
	
	protected boolean on;
    protected String gridName;

    public abstract double getHourlyProduction(Object... weatherConditions);

    public String getGridName(){
        return gridName;
    }

    public void setGridName(String gridName){
        this.gridName = gridName;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public String toString() {
        return "PowerPlant [on=" + on + "]";
    }

}

