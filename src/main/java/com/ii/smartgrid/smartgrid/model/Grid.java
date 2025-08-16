package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;

public class Grid extends CustomObject {

    private List<String> smartBuildingNames;
    private Map<String, EnergyTransaction> smartBuildingsWithoutPower;
    private Map<String, EnergyTransaction> smartBuildingsEnergyRequests;
    private List<String> renewablePowerPlantNames;
    private Map<String, Boolean> nonRenewablePowerPlantActiveStatus;
    private List<DistributionInstruction> distributionInstructions;
    private List<String> gridNames;
    private double expectedConsumption;
    private double expectedProduction;
    private String loadManagerName;
    private int numberOfMessagesToReceive;
    private Priority priority;
    private Battery battery;

    public Grid() {
        smartBuildingNames = new ArrayList<>();
        smartBuildingsWithoutPower = new HashMap<>();
        smartBuildingsEnergyRequests = new HashMap<>();
        renewablePowerPlantNames = new ArrayList<>();
        nonRenewablePowerPlantActiveStatus = new HashMap<>();
        distributionInstructions = new ArrayList<>();
        gridNames = new ArrayList<>();
        expectedConsumption = 0;
        this.priority = Priority.LOW;
    }

    public void addEnergyRequest(String smartmeBuildingName, EnergyTransaction request) {
        smartBuildingsEnergyRequests.put(smartmeBuildingName, request);
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

    public Map<String, Boolean> getNonRenewablePowerPlantActiveStatus() {
        return nonRenewablePowerPlantActiveStatus;
    }

    public void setNonRenewablePowerPlantActiveStatus(Map<String, Boolean> nonRenewablePowerPlantActiveStatus) {
        this.nonRenewablePowerPlantActiveStatus = nonRenewablePowerPlantActiveStatus;
    }

    public List<String> getNonRenewablePowerPlantNames() {
        return new ArrayList<>(nonRenewablePowerPlantActiveStatus.keySet());
    }

    // Initialize all nonRenewablePowerPlants status
    public void setNonRenewablePowerPlantNames(List<String> nonRenewablePowerPlantNames) {
        for (String nonRenewablePowerPlantName : nonRenewablePowerPlantNames) {
            nonRenewablePowerPlantActiveStatus.put(nonRenewablePowerPlantName, false);
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
        distributionInstructions.clear();
    }

    @Override
    public String toString() {
        return "Grid [smartBuildingNames=" + smartBuildingNames + ", smartBuildingsWithoutPower=" + smartBuildingsWithoutPower
                + ", smartBuildingsEnergyRequests=" + smartBuildingsEnergyRequests + ", renewablePowerPlantNames="
                + renewablePowerPlantNames
                + ", nonRenewablePowerPlantActiveStatus=" + nonRenewablePowerPlantActiveStatus
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

    // Update the nonRenewablePowerPlant status
    public void updateNonRenewablePowerPlantActiveStatus(List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos) {
        for (NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo : nonRenewablePowerPlantInfos) {
            String name = nonRenewablePowerPlantInfo.getName();
            boolean status = nonRenewablePowerPlantInfo.isOn();
            if (nonRenewablePowerPlantActiveStatus.containsKey(name)) {
                nonRenewablePowerPlantActiveStatus.put(name, status);
            }
        }
    }

}
