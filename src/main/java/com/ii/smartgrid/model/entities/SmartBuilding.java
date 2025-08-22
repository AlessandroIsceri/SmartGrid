package com.ii.smartgrid.model.entities;

import java.util.ArrayList;
import java.util.List;

import com.ii.smartgrid.agents.SmartBuildingAgent.SmartBuildingStatus;
import com.ii.smartgrid.model.Battery;
import com.ii.smartgrid.model.building.Appliance;
import com.ii.smartgrid.model.building.BuildingPhotovoltaicSystem;
import com.ii.smartgrid.model.building.Routine;
import com.ii.smartgrid.model.building.Task;
import com.ii.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.utils.WeatherUtil.WeatherStatus;


public class SmartBuilding extends CustomObject {
    private List<Appliance> appliances;
    private BuildingPhotovoltaicSystem buildingPhotovoltaicSystem;
    private Battery battery;
    private Routine routine;
    private double expectedConsumption;
    private double expectedProduction;
    private double nextTurnExpectedConsumption;
    private String gridName;
    private Priority priority;

    public SmartBuilding() {
        super();
        appliances = new ArrayList<>();
    }

    public boolean canBeRestored() {
        return this.battery.getMaxCapacityInWattHour() * 0.5 < this.battery.getStoredEnergy() + this.expectedProduction;
    }

    public void fillBattery(double extraEnergy) {
        if (battery != null) {
            battery.fillBattery(extraEnergy);
        }
    }

    public void followRoutine(int curTurn, WeatherStatus curWeather, SmartBuildingStatus smartBuildingStatus) {
        expectedConsumption = 0;
        double turnDurationHours = TimeUtils.getTurnDurationHours();

        for(Appliance appliance : appliances){
            if(appliance.isAlwaysOn()){
                expectedConsumption += appliance.getHourlyConsumption() * turnDurationHours;
            }
        }

        for (Task curTask : routine.getTasks()) {
            // Get the start turn and end turn for current task
            int startTurn = TimeUtils.convertTimeToTurn(curTask.getStartTime());
            int endTurn = TimeUtils.convertTimeToTurn(curTask.getEndTime());
            String applianceName = curTask.getApplianceName();
            curTurn = curTurn % TimeUtils.getDailyTurnsNumber();
            // Find the appliance with the given name using stream
            Appliance curAppliance = appliances.stream().filter(appliance -> appliance.getName().equals(applianceName)).findFirst().get();
            if (endTurn == curTurn) {
                // The appliance must be turned off this turn
                curAppliance.setOn(false);
            }
            if (startTurn == curTurn) {
                // The appliance must be turned on this turn
                curAppliance.setOn(true);
            } 
            if(curAppliance.isOn() && !curAppliance.isAlwaysOn()){
                expectedConsumption += curAppliance.getHourlyConsumption() * turnDurationHours;
            }
        }
        expectedProduction = 0;
        if (buildingPhotovoltaicSystem != null) {
            expectedProduction = buildingPhotovoltaicSystem.getHourlyProduction(curWeather, curTurn) * TimeUtils.getTurnDurationHours();
        }
        if (battery != null) {
            expectedProduction = expectedProduction + battery.getAvailableEnergy();
        }
    }

    public void predictNextTurnConsumptionRoutine(int nextTurn, SmartBuildingStatus smartBuildingStatus) {
        nextTurnExpectedConsumption = 0;
        
        double turnDurationHours = TimeUtils.getTurnDurationHours();

        for(Appliance appliance : appliances){
            if(appliance.isAlwaysOn()){
                nextTurnExpectedConsumption += appliance.getHourlyConsumption() * turnDurationHours;
            }
        }

        for (Task curTask : routine.getTasks()) {
            // Get the start turn and end turn for current task
            int startTurn = TimeUtils.convertTimeToTurn(curTask.getStartTime());
            int endTurn = TimeUtils.convertTimeToTurn(curTask.getEndTime());
            String applianceName = curTask.getApplianceName();
            nextTurn = nextTurn % TimeUtils.getDailyTurnsNumber();
            // Find the appliance with the given name using stream
            Appliance curAppliance = appliances.stream().filter(appliance -> appliance.getName().equals(applianceName)).findFirst().get();
            
            if(startTurn <= nextTurn && endTurn > nextTurn){
                nextTurnExpectedConsumption += curAppliance.getHourlyConsumption() * turnDurationHours;
            }
        }
    }


    public List<Appliance> getAppliances() {
        return appliances;
    }

    public void setAppliances(List<Appliance> appliances) {
        this.appliances = appliances;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    public BuildingPhotovoltaicSystem getBuildingPhotovoltaicSystem() {
        return buildingPhotovoltaicSystem;
    }

    public void setBuildingPhotovoltaicSystem(BuildingPhotovoltaicSystem buildingPhotovoltaicSystem) {
        this.buildingPhotovoltaicSystem = buildingPhotovoltaicSystem;
    }

    public double getExpectedConsumption() {
        return expectedConsumption;
    }

    public void setExpectedConsumption(double expectedConsumption) {
        this.expectedConsumption = expectedConsumption;
    }

    public double getExpectedProduction() {
        return expectedProduction;
    }

    public void setExpectedProduction(double expectedProduction) {
        this.expectedProduction = expectedProduction;
    }

    public String getGridName() {
        return gridName;
    }

    public void setGridName(String gridName) {
        this.gridName = gridName;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Routine getRoutine() {
        return routine;
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }

    public void restorePower(double energy) {
        if (battery != null) {
            battery.fillBattery(energy);
        }
        for (Appliance appliance : appliances) {
            if (appliance.isAlwaysOn()) {
                appliance.setOn(true);
            }
        }
    }

    public void shutDown() {
        for (Appliance appliance : appliances) {
            appliance.setOn(false);
        }
        expectedConsumption = 0;
    }

    public double getNextTurnExpectedConsumption() {
        return nextTurnExpectedConsumption;
    }

    public void setNextTurnExpectedConsumption(double nextTurnExpectedConsumption) {
        this.nextTurnExpectedConsumption = nextTurnExpectedConsumption;
    }

    @Override
    public String toString() {
        return "SmartBuilding [appliances=" + appliances + ", buildingPhotovoltaicSystem=" + buildingPhotovoltaicSystem + ", battery=" + battery
                + ", routine=" + routine + ", expectedConsumption=" + expectedConsumption
                + ", expectedProduction=" + expectedProduction + ", gridName=" + gridName + "]";
    }

}
