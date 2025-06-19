package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.DistributeEnergyBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ManageEnergyRequestBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ReceiveLoadManagerAnswersBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.RequestEnergyBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.RestoreHomesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.FollowRoutine;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class Grid extends CustomAgent{

	private double currentEnergy;
	private double maxCapacity;
	private List<String> smartHomeNames;
	private List<String> powerPlantNames;
	private Map<String, Double> smartHomesWithoutPower;
    private Map<String, Double> smartHomesEnergyRequests;
    private double expectedConsumption;
	private String loadManagerName;

	@Override
    public void setup() {
		Object[] args = this.getArguments();
		smartHomeNames = new ArrayList<String>();
		smartHomesWithoutPower = new LinkedHashMap<String, Double>();
        smartHomesEnergyRequests = new LinkedHashMap<String, Double>();
		boolean readingSmartHomes = true;
		
		for(int i = 0; i < args.length - 3; i++) {
			String curArg = ((String) args[i]);
            smartHomeNames.add(curArg);
		}
		
		this.loadManagerName = (String) args[args.length - 3];
		this.maxCapacity = Double.parseDouble((String) args[args.length - 2]);
		this.currentEnergy = Double.parseDouble((String) args[args.length - 1]);

        addBehaviour(new GridBehaviour(this));
        this.log("Setup completed");        
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

	public List<String> getPowerPlantNames() {
		return powerPlantNames;
	}

	public void setPowerPlantNames(List<String> powerPlantNames) {
		this.powerPlantNames = powerPlantNames;
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

    @Override
	public String toString() {
		return "Grid [currentEnergy=" + currentEnergy + ", maxCapacity=" + maxCapacity + ", smartHomeNames="
				+ smartHomeNames + ", powerPlantNames=" + powerPlantNames + "]";
	}

	public boolean consumeEnergy(double requestedEnergy) {
		if(currentEnergy >= requestedEnergy) {
			currentEnergy -= requestedEnergy;
            this.log("currentEnergy: " + currentEnergy);
            return true;
		}
        this.log("(LOW ENERGY IN GRID) currentEnergy: " + currentEnergy);
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

	// public Map<String, Double> getHomesRestoreInfo(){
	// 	// FIFO queue for restoring power
    //     Map<String, Double> ret = new LinkedHashMap<String, Double>();
	// 	for(String key : smartHomesWithoutPower.keySet()){
    //         double requestedEnergy = smartHomesWithoutPower.get(key);
	// 		if(currentEnergy > requestedEnergy){
    //             currentEnergy -= requestedEnergy;
	// 			ret.put(key, requestedEnergy);
    //         }
	// 	}
    //     return ret;
	// }

    public int getSmartHomeWithoutPowerSize(){
        return smartHomesWithoutPower.size();
    }

    private class GridBehaviour extends GenericTurnBehaviour{

        public GridBehaviour(Grid grid) {
            super(grid);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            //ricevi le richieste (o se in blackout o meno una casa)
            //chiedi energia al load manager
            //aspetta risposta load manager x sapere quanta energia arriverà
            //invia le risposte
            //aspetta i messaggi di blackout
            //restora le case se riesci
            sequentialTurnBehaviour.addSubBehaviour(new ManageEnergyRequestBehaviour((Grid) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new RequestEnergyBehaviour((Grid) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveLoadManagerAnswersBehaviour((Grid) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new DistributeEnergyBehaviour((Grid) myAgent));
            // sequentialTurnBehaviour.addSubBehaviour(new ManageBlackoutBehaviour((Grid) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new RestoreHomesBehaviour((Grid) myAgent));
            ((Grid) myAgent).setExpectedConsumption(getBlackoutEnergyRequest());
        }

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
