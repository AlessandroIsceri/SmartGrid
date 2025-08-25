package com.ii.smartgrid.model.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.model.Battery;
import com.ii.smartgrid.model.Cable;
import com.ii.smartgrid.model.NonRenewablePowerPlantInfo;
import com.ii.smartgrid.model.routing.DistributionInstruction;
import com.ii.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.model.routing.EnergyTransaction.TransactionType;

public class Grid extends CustomObject {

    private List<String> smartBuildingNames;
    private Map<String, EnergyTransaction> smartBuildingsWithoutPower;
    private Map<String, EnergyTransaction> smartBuildingsEnergyRequests;
    private List<String> renewablePowerPlantNames;
    private Map<String, Boolean> nonRenewablePowerPlantActiveState;
    private List<DistributionInstruction> distributionInstructions;
    private List<String> gridNames;
    private double expectedConsumption;
    private double expectedProduction;
    private double nextTurnExpectedConsumption;
    private String loadManagerName;
    private int numberOfMessagesToReceive;
    private Priority priority;
    private Battery battery;
    private double expectedRenewableProduction;

    public Grid() {
        smartBuildingNames = new ArrayList<>();
        smartBuildingsWithoutPower = new HashMap<>();
        smartBuildingsEnergyRequests = new HashMap<>();
        renewablePowerPlantNames = new ArrayList<>();
        nonRenewablePowerPlantActiveState = new HashMap<>();
        distributionInstructions = new ArrayList<>();
        gridNames = new ArrayList<>();
        expectedConsumption = 0;
        expectedProduction = 0;
        expectedRenewableProduction = 0;
        nextTurnExpectedConsumption = 0;
        this.priority = Priority.LOW;
    }

    public void addEnergyRequest(String smartBuildingName, EnergyTransaction request) {
        smartBuildingsEnergyRequests.put(smartBuildingName, request);
    }

    public void addExpectedConsumption(double energy) {
        expectedConsumption += energy;
    }

    public void addExpectedProduction(double energy) {
        expectedProduction += energy;
    }

    public void addSmartBuildingWithoutPower(String smartBuildingName, EnergyTransaction energyTransaction) {
        smartBuildingsWithoutPower.put(smartBuildingName, energyTransaction);
    }

    public boolean containsSmartBuildingWithoutPower(String smartBuildingName) {
        return smartBuildingsWithoutPower.containsKey(smartBuildingName);
    }

    public double fillBattery(double extraEnergy) {
        return battery.fillBattery(extraEnergy);
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    // Gets the energy requests coming from blackout buildings
    public double getBlackoutEnergyRequest() {
        double sum = 0;
        for (EnergyTransaction energyTransaction : smartBuildingsWithoutPower.values()) {
            sum += energyTransaction.getEnergyTransactionValue();
        }
        return sum;
    }

    // Gets the blackout buildings in that priority group 
    public List<EnergyTransaction> getBlackoutSmartBuildingsEnergyRequestsByPriority(Priority priority) {
        List<EnergyTransaction> results = new ArrayList<>();
        for (EnergyTransaction energyTransaction : smartBuildingsWithoutPower.values()) {
            if (energyTransaction.getPriority() == priority) {
                results.add(energyTransaction);
            }
        }
        return results;
    }

    // Gets the requested energy of the buildings
    public double getBuildingRequestedEnergy() {
        double sum = 0;
        for (EnergyTransaction energyTransaction : smartBuildingsEnergyRequests.values()) {
            if (energyTransaction.getTransactionType() == TransactionType.RECEIVE) {
                sum += energyTransaction.getEnergyTransactionValue();
            }
        }
        for (EnergyTransaction energyTransaction : smartBuildingsWithoutPower.values()) {
            if (energyTransaction.getTransactionType() == TransactionType.RECEIVE) {
                sum += energyTransaction.getEnergyTransactionValue();
            }
        }
        return sum;
    }

    // Get the list of cables of the connected grids
    public List<Cable> getConnectedGridsCables() {
        List<Cable> connectedGridsCables = new ArrayList<>();
        for (Cable cable : connectedAgents.values()) {
            if (cable.getTo().contains("Grid")) {
                connectedGridsCables.add(cable);
            }
        }
        return connectedGridsCables;
    }

    public List<DistributionInstruction> getDistributionInstructions() {
        return distributionInstructions;
    }

    public void setDistributionInstructions(List<DistributionInstruction> distributionInstructions) {
        this.distributionInstructions = distributionInstructions;
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

    public List<String> getGridNames() {
        return gridNames;
    }

    public void setGridNames(List<String> gridNames) {
        this.gridNames = gridNames;
    }

    public String getLoadManagerName() {
        return loadManagerName;
    }

    public void setLoadManagerName(String loadManagerName) {
        this.loadManagerName = loadManagerName;
    }

    public Map<String, Boolean> getNonRenewablePowerPlantActiveState() {
        return nonRenewablePowerPlantActiveState;
    }

    public void setNonRenewablePowerPlantActiveState(Map<String, Boolean> nonRenewablePowerPlantActiveState) {
        this.nonRenewablePowerPlantActiveState = nonRenewablePowerPlantActiveState;
    }

    public List<String> getNonRenewablePowerPlantNames() {
        return new ArrayList<>(nonRenewablePowerPlantActiveState.keySet());
    }

    // Initialize all nonRenewablePowerPlants state
    public void setNonRenewablePowerPlantNames(List<String> nonRenewablePowerPlantNames) {
        for (String nonRenewablePowerPlantName : nonRenewablePowerPlantNames) {
            nonRenewablePowerPlantActiveState.put(nonRenewablePowerPlantName, false);
        }
    }

    public int getNumberOfMessagesToReceive() {
        return this.numberOfMessagesToReceive;
    }

    public void setNumberOfMessagesToReceive(int numberOfMessagesToReceive) {
        this.numberOfMessagesToReceive = numberOfMessagesToReceive;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public List<String> getRenewablePowerPlantNames() {
        return renewablePowerPlantNames;
    }

    public void setRenewablePowerPlantNames(List<String> renewablePowerPlantNames) {
        this.renewablePowerPlantNames = renewablePowerPlantNames;
    }

    public List<String> getSmartBuildingNames() {
        return smartBuildingNames;
    }

    public void setSmartBuildingNames(List<String> smartBuildingNames) {
        this.smartBuildingNames = smartBuildingNames;
    }

    public int getSmartBuildingWithoutPowerSize() {
        return smartBuildingsWithoutPower.size();
    }

    public Map<String, EnergyTransaction> getSmartBuildingsEnergyRequests() {
        return smartBuildingsEnergyRequests;
    }

    public void setSmartBuildingsEnergyRequests(Map<String, EnergyTransaction> smartBuildingsEnergyRequests) {
        this.smartBuildingsEnergyRequests = smartBuildingsEnergyRequests;
    }

    // Get SmartBuilding requests with that priority
    public List<EnergyTransaction> getSmartBuildingsEnergyRequestsByPriority(Priority priority) {
        List<EnergyTransaction> results = new ArrayList<>();
        for (EnergyTransaction energyTransaction : smartBuildingsEnergyRequests.values()) {
            if (energyTransaction.getPriority() == priority && energyTransaction.getTransactionType() == TransactionType.RECEIVE) {
                results.add(energyTransaction);
            }
        }
        return results;
    }

    public Map<String, EnergyTransaction> getSmartBuildingsWithoutPower() {
        return smartBuildingsWithoutPower;
    }

    public void setSmartBuildingsWithoutPower(Map<String, EnergyTransaction> smartBuildingsWithoutPower) {
        this.smartBuildingsWithoutPower = smartBuildingsWithoutPower;
    }

    public void removeEnergyRequest(String smartBuildingName) {
        smartBuildingsEnergyRequests.remove(smartBuildingName);
    }

    public void removeSmartBuildingWithoutPower(String smartBuildingName) {
        smartBuildingsWithoutPower.remove(smartBuildingName);
    }

    public void resetValues() {
        this.expectedConsumption = 0;
        this.expectedProduction = 0;
        this.nextTurnExpectedConsumption = 0;
        this.expectedRenewableProduction = 0;
        distributionInstructions.clear();
    }

    @Override
    public String toString() {
        return "Grid [smartBuildingNames=" + smartBuildingNames + ", smartBuildingsWithoutPower=" + smartBuildingsWithoutPower
                + ", smartBuildingsEnergyRequests=" + smartBuildingsEnergyRequests + ", renewablePowerPlantNames="
                + renewablePowerPlantNames
                + ", nonRenewablePowerPlantActiveState=" + nonRenewablePowerPlantActiveState
                + ", distributionInstructions=" + distributionInstructions + ", gridNames=" + gridNames
                + ", expectedConsumption=" + expectedConsumption + ", expectedProduction=" + expectedProduction
                + ", loadManagerName=" + loadManagerName + ", numberOfMessagesToReceive=" + numberOfMessagesToReceive
                + ", priority=" + priority + ", battery=" + battery + "]";
    }

    public void updateGridPriority(Priority priority) {
        if (priority.ordinal() < this.priority.ordinal()) {
            this.priority = priority;
        }
    }

    // Update the nonRenewablePowerPlant state
    public void updateNonRenewablePowerPlantActiveState(List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos) {
        for (NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo : nonRenewablePowerPlantInfos) {
            String name = nonRenewablePowerPlantInfo.getName();
            boolean state = nonRenewablePowerPlantInfo.isOn();
            if (nonRenewablePowerPlantActiveState.containsKey(name)) {
                nonRenewablePowerPlantActiveState.put(name, state);
            }
        }
    }

    public void removeExpectedConsumption(double energyTransactionValue) {
        expectedConsumption -= energyTransactionValue;
    }

    public void addExpectedRenewableProduction(double renewableProduction){
        this.expectedRenewableProduction += renewableProduction;
    }

	public void turnOnAllNonRenewablePowerPlants() {
		for (String nonRenewablePowerPlantName : nonRenewablePowerPlantActiveState.keySet()) {
            nonRenewablePowerPlantActiveState.put(nonRenewablePowerPlantName, true);
        }
	}

    public double getNextTurnExpectedConsumption() {
        return nextTurnExpectedConsumption;
    }

    public void addNextTurnExpectedConsumption(double expectedConsumption) {
        this.nextTurnExpectedConsumption += expectedConsumption;
    }

	public void updateSmartBuildingsWithoutPower(String sender, double nextTurnExpectedConsumption) {
        EnergyTransaction energyTransaction = smartBuildingsWithoutPower.get(sender);
        energyTransaction.setEnergyTransactionValue(nextTurnExpectedConsumption);
	}

    public double getExpectedRenewableProduction() {
        return expectedRenewableProduction;
    }


    
}
