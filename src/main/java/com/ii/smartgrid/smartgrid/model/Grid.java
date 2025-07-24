package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ReceiveEnergyFromLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ReceiveEnergyRequestsFromSmartHomesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.SendEnergyRequestToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.SendEnergyToSmartHomesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.SendRestoreMessagesToSmartHomesBehaviour;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class Grid extends CustomObject{

	private double currentEnergy;
	private double maxCapacity;
	private List<String> smartHomeNames;
	private Map<String, Double> smartHomesWithoutPower;
    private Map<String, Double> smartHomesEnergyRequests;
    private double expectedConsumption;
	private String loadManagerName;

	public Grid(){
        smartHomesWithoutPower = new HashMap<String, Double>();       
        smartHomesEnergyRequests = new HashMap<String, Double>();
        expectedConsumption = 0;
    }

	public double addEnergy(double newEnergy) {
		if(currentEnergy + newEnergy <= maxCapacity){
			currentEnergy = currentEnergy + newEnergy;
			return 0;
		} else {
			double excess = newEnergy - (maxCapacity - currentEnergy);
			currentEnergy = maxCapacity;
			return excess;
		}
	}
	
	public double getCurrentEnergy() {
		return currentEnergy;
	}

	public void setCurrentEnergy(double currentEnergy) {
		this.currentEnergy = currentEnergy;
	}

	public double getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(double maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public List<String> getSmartHomeNames() {
		return smartHomeNames;
	}

	public void setSmartHomeNames(List<String> smartHomeNames) {
		this.smartHomeNames = smartHomeNames;
	}

    public Map<String, Double> getSmartHomesEnergyRequests() {
        return smartHomesEnergyRequests;
    }

    public void setSmartHomesEnergyRequests(Map<String, Double> smartHomesEnergyRequests) {
        this.smartHomesEnergyRequests = smartHomesEnergyRequests;
    }

    public void addEnergyRequest(String smartmeHomeName, double request){
        smartHomesEnergyRequests.put(smartmeHomeName, request);
    }

	public void removeEnergyRequest(String smartHomeName){
		smartHomesEnergyRequests.remove(smartHomeName);
	}

	public boolean consumeEnergy(double requestedEnergy) {
		if(currentEnergy >= requestedEnergy) {
			currentEnergy -= requestedEnergy;
            return true;
		}
        return false;
	}

	public double getExpectedConsumption() {
        return expectedConsumption;
    }

    public void setExpectedConsumption(double expectedConsumption) {
        this.expectedConsumption = expectedConsumption;
    }

    public boolean containsSmartHomeWithoutPower(String smartHomeName){
		return smartHomesWithoutPower.containsKey(smartHomeName);
	}

	public void addSmartHomeWithoutPower(String smartHomeName, double energy){
		smartHomesWithoutPower.put(smartHomeName, energy);
	}

	public void removeSmartHomeWithoutPower(String smartHomeName){
		smartHomesWithoutPower.remove(smartHomeName);
	}

    public int getSmartHomeWithoutPowerSize(){
        return smartHomesWithoutPower.size();
    }

    public void removeExpectedConsumption(double energy) {
        expectedConsumption -= energy;
    }

    public double getBlackoutEnergyRequest() {
        double sum = 0;
        for(String smartHome : smartHomesWithoutPower.keySet()){
            sum += smartHomesWithoutPower.get(smartHome);
        }
        return sum; 
    }

    public void addExpectedConsumption(double energy) {
        expectedConsumption += energy;
    }

	public String getLoadManagerName(){
		return loadManagerName;
	}

	public void setLoadManagerName(String loadManagerName){
		this.loadManagerName = loadManagerName;
	}

    public Map<String, Double> getSmartHomesWithoutPower() {
        return smartHomesWithoutPower;
    }


}
