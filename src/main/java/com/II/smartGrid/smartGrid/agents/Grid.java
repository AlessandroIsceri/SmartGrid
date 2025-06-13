package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.DistributeEnergy;
import com.ii.smartgrid.smartgrid.behaviours.grid.ManageBlackoutBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.FollowRoutine;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class Grid extends CustomAgent{

	private double currentEnergy;
	private double maxCapacity;
	private List<String> smartHomeNames;
	private List<String> powerPlantNames;
	private List<String> smartHomesWithoutPower;

	@Override
    public void setup() {
		Object[] args = this.getArguments();
		smartHomeNames = new ArrayList<String>();
		powerPlantNames = new ArrayList<String>();
		smartHomesWithoutPower = new ArrayList<String>();
		boolean readingSmartHomes = true;
		
		for(int i = 0; i < args.length - 2; i++) {
			String curArg = ((String) args[i]);
			if(curArg.equals("**")) {
				readingSmartHomes = false;
			}else {
				if(readingSmartHomes){
					smartHomeNames.add(curArg);
				} else {
					powerPlantNames.add(curArg);
				}
			}
		}
		
		this.maxCapacity = Double.parseDouble((String) args[args.length - 1]);
		this.currentEnergy = Double.parseDouble((String) args[args.length - 2]);
        
        //behaviour per comunicare con le case
        //behaviour per comunicare con le powerplant
        addBehaviour(new DistributeEnergy(this));
        addBehaviour(new ManageBlackoutBehaviour(this));
        
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

	public boolean containsSmartHomeWithoutPower(String smartHomeName){
		return smartHomesWithoutPower.contains(smartHomeName);
	}

	public void addSmartHomeWithoutPower(String smartHomeName){
		smartHomesWithoutPower.add(smartHomeName);
	}

	public void removeSmartHomeWithoutPower(String smartHomeName){
		smartHomesWithoutPower.remove(smartHomeName);
	}

    // private class GridBehaviour extends GenericTurnBehaviour{

    //     public GridBehaviour(Grid grid) {
    //         super(grid);
    //     }

    //     @Override
    //     protected void executeTurn(ACLMessage replyMessage, SequentialBehaviour sequentialTurnBehaviour) {
    //         sequentialTurnBehaviour.addSubBehaviour(new));
    //     }

    // }

}
