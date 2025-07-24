package com.ii.smartgrid.smartgrid.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.CheckSmartHomeMessagesBehaviour;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

public class HydroPowerPlant extends RenewablePowerPlant{
    
    private double efficiency;
    private double flowRate; // m^3/s
    private double headHeight; // m

    private final double WATER_DENSITY = 1000; // kg/m^3
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