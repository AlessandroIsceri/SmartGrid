package com.ii.smartgrid.smartgrid.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.CheckSmartHomeMessagesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.ReceiveEnergyFromGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.SendEnergyRequestToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.SendEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.WaitForRestoreBehaviour;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class SmartHome extends CustomObject{
	private List<Appliance> appliances;
	private HomePhotovoltaicSystem homePhotovoltaicSystem;
	private Battery battery;
	private double maxPower;
	private Routine routine;
	private double expectedConsumption;
    private double expectedProduction;
	private String gridName;

	public enum SmartHomeStatus {BLACKOUT, WORKING};
	private SmartHomeStatus status = SmartHomeStatus.WORKING;

    public SmartHome(){
        super();
    }

	public List<Appliance> getAppliances() {
		return appliances;
	}

	public double getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	public Routine getRoutine() {
		return routine;
	}

	public void setRoutine(Routine routine) {
		this.routine = routine;
	}

	public void setAppliances(List<Appliance> appliances) {
		this.appliances = appliances;
	}
	
	public double getExpectedConsumption() {
		return expectedConsumption;
	}

	public void setExpectedConsumption(double expectedConsumption) {
		this.expectedConsumption = expectedConsumption;
	}

	public Battery getBattery() {
		return battery;
	}

	public void setBattery(Battery battery) {
		this.battery = battery;
	}
	
	public String getGridName() {
		return gridName;
	}

	public void setStatus(SmartHomeStatus status) {
        this.status = status;
    }

    public void setGridName(String gridName) {
		this.gridName = gridName;
	}

	@Override
    public String toString() {
        return "SmartHome [appliances=" + appliances + ", homePhotovoltaicSystem=" + homePhotovoltaicSystem + ", battery=" + battery
                + ", maxPower=" + maxPower + ", routine=" + routine + ", expectedConsumption=" + expectedConsumption
                + ", expectedProduction=" + expectedProduction + ", gridName=" + gridName + ", status=" + status + "]";
    }

    public double getAvailableEnergy(){
        if(battery != null){
            return expectedProduction += battery.getStoredEnergy();
        }
		return expectedProduction;
	}


	public double getExpectedProduction() {
        return expectedProduction;
    }

    public void setExpectedProduction(double expectedProduction) {
        this.expectedProduction = expectedProduction;
    }

    public void shutDown(){
		for(Appliance appliance: appliances){
			appliance.setOn(false);
		}
        expectedConsumption = 0;
        status = SmartHomeStatus.BLACKOUT;
    }

	public void restorePower(double energy){
		if(battery != null){
			double extra = battery.fillBattery(energy);
		}
		for(Appliance appliance: appliances){
			if(appliance.isAlwaysOn()){
				appliance.setOn(true);
                expectedConsumption += appliance.getHourlyConsumption() * TimeUtils.getTurnDurationHours();
			}
		}
        status = SmartHomeStatus.WORKING;
	}

    public SmartHomeStatus getStatus() {
        return status;
    }

    public void followRoutine(int curTurn, WeatherStatus curWeather){
        expectedConsumption = 0;
		expectedProduction = 0;

		int turnDurationHours = TimeUtils.getTurnDurationHours();
		for(Task curTask : routine.getTasks()) {
			int startTurn = TimeUtils.convertTimeToTurn(curTask.getStartTime());
			int endTurn = TimeUtils.convertTimeToTurn(curTask.getEndTime());
			if(startTurn == curTurn) {
				curTask.getAppliance().setOn(true);
				expectedConsumption += curTask.getAppliance().getHourlyConsumption() * turnDurationHours;
				// log("Turn dur: " + turnDuration + " - " + "adding consumption: " + curTask.getAppliance().getHourlyConsumption());
			} else if(endTurn == curTurn) {
				curTask.getAppliance().setOn(false);
				expectedConsumption -= curTask.getAppliance().getHourlyConsumption() * turnDurationHours;
				// log("Turn dur: " + turnDuration + " - " + "removing consumption: " + curTask.getAppliance().getHourlyConsumption());
			}
		}
		
		expectedProduction += homePhotovoltaicSystem.getHourlyProduction(curWeather, curTurn) * turnDurationHours;
		
		
        // log("expectedProduction: " + expectedProduction);
        // log("expectedConsumption: " + expectedConsumption);
    }

    public HomePhotovoltaicSystem getHomePhotovoltaicSystem() {
        return homePhotovoltaicSystem;
    }

    public void setHomePhotovoltaicSystem(HomePhotovoltaicSystem homePhotovoltaicSystem) {
        this.homePhotovoltaicSystem = homePhotovoltaicSystem;
    }

    

}
