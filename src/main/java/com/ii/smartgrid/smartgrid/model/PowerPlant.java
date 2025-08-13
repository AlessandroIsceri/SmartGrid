package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;

public abstract class PowerPlant extends CustomObject{
	
	protected boolean on;
    protected String gridName;
    // protected double curTurnExpectedProduction;


    public abstract double getHourlyProduction(Object... weatherConditions);


	

    public double getCurTurnExpectedProduction() {
        return getHourlyProduction() * TimeUtils.getTurnDurationHours();
    }

    // public void setCurTurnExpectedProduction(double curTurnExpectedProduction) {
    //     this.curTurnExpectedProduction = curTurnExpectedProduction;
    // }

    
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

