package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent.SmartHomeStatus;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;


public class SmartHome extends CustomObject{
	private List<Appliance> appliances;
	private HomePhotovoltaicSystem homePhotovoltaicSystem;
	private Battery battery;
	private Routine routine;
	private double expectedConsumption;
    private double expectedProduction;
	private String gridName;
    private Priority priority;

    public SmartHome(){
        super();
        appliances = new ArrayList<Appliance>();
    }

	public List<Appliance> getAppliances() {
		return appliances;
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

    public void setGridName(String gridName) {
		this.gridName = gridName;
	}

	@Override
    public String toString() {
        return "SmartHome [appliances=" + appliances + ", homePhotovoltaicSystem=" + homePhotovoltaicSystem + ", battery=" + battery
                + ", routine=" + routine + ", expectedConsumption=" + expectedConsumption
                + ", expectedProduction=" + expectedProduction + ", gridName=" + gridName + "]";
    }

    // public double getAvailableEnergy(){
    //     if(battery != null){
    //         return expectedProduction + battery.getStoredEnergy();
    //     }
	// 	return expectedProduction;
	// }


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
	}

    public void followRoutine(int curTurn, WeatherStatus curWeather, SmartHomeStatus smartHomeStatus){
        // expectedConsumption = 0;

        if(smartHomeStatus != SmartHomeStatus.BLACKOUT){
            double turnDurationHours = TimeUtils.getTurnDurationHours();
            for(Task curTask : routine.getTasks()) {
                int startTurn = TimeUtils.convertTimeToTurn(curTask.getStartTime());
                int endTurn = TimeUtils.convertTimeToTurn(curTask.getEndTime());
                if(startTurn == curTurn) {
                    curTask.getAppliance().setOn(true);
                    expectedConsumption += curTask.getAppliance().getHourlyConsumption() * turnDurationHours;
                } else if(endTurn == curTurn) {
                    curTask.getAppliance().setOn(false);
                    expectedConsumption -= curTask.getAppliance().getHourlyConsumption() * turnDurationHours;
                }
            }
        }
        expectedProduction = 0;
        if(homePhotovoltaicSystem != null){
		    expectedProduction = homePhotovoltaicSystem.getHourlyProduction(curWeather, curTurn) * TimeUtils.getTurnDurationHours();
        }
        if(battery != null){
            expectedProduction = expectedProduction + battery.getAvailableEnergy();
        }
    }
    // public double computeExpectedProduction(int curTurn, WeatherStatus curWeather){
    //     return homePhotovoltaicSystem.getHourlyProduction(curWeather, curTurn) * TimeUtils.getTurnDurationHours();
    // }

    public HomePhotovoltaicSystem getHomePhotovoltaicSystem() {
        return homePhotovoltaicSystem;
    }

    public void setHomePhotovoltaicSystem(HomePhotovoltaicSystem homePhotovoltaicSystem) {
        this.homePhotovoltaicSystem = homePhotovoltaicSystem;
    }

    public boolean canBeRestored(int curTurn, WeatherStatus curWeather) {
        return this.battery.getMaxCapacityInWatt() * 0.5 < this.battery.getStoredEnergy() + this.expectedProduction;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

}
