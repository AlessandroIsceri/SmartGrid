package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;

public class Grid extends CustomObject {

    // private double currentEnergy;
    // private double maxCapacity;
    private List<String> smartBuildingNames;
    private Map<String, EnergyTransaction> smartBuildingsWithoutPower;
    private Map<String, EnergyTransaction> smartBuildingsEnergyRequests;
    private List<String> renewablePowerPlantNames;
    //private List<String> nonRenewablePowerPlantNames;
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
        smartBuildingNames = new ArrayList<String>();
        smartBuildingsWithoutPower = new HashMap<String, EnergyTransaction>();
        smartBuildingsEnergyRequests = new HashMap<String, EnergyTransaction>();
        renewablePowerPlantNames = new ArrayList<String>();
        // nonRenewablePowerPlantNames = new ArrayList<String>();
        nonRenewablePowerPlantActiveStatus = new HashMap<String, Boolean>();
        distributionInstructions = new ArrayList<DistributionInstruction>();
        gridNames = new ArrayList<String>();
        expectedConsumption = 0;
        this.priority = priority.LOW;
    }

    public void updateNonRenewablePowerPlantActiveStatus(List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos) {
        for (NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo : nonRenewablePowerPlantInfos) {
            String name = nonRenewablePowerPlantInfo.getName();
            boolean status = nonRenewablePowerPlantInfo.isOn();
            // if(nonRenewablePowerPlantNames.contains(name)){
            //     nonRenewablePowerPlantActiveStatus.put(name, status);
            // }
            if(nonRenewablePowerPlantActiveStatus.containsKey(name)){
                nonRenewablePowerPlantActiveStatus.put(name, status);
            }
        }
    }

    public List<DistributionInstruction> getDistributionInstructions() {
        return distributionInstructions;
    }

    public void setDistributionInstructions(List<DistributionInstruction> distributionInstructions) {
        this.distributionInstructions = distributionInstructions;
    }

    public Map<String, Boolean> getNonRenewablePowerPlantActiveStatus() {
        return nonRenewablePowerPlantActiveStatus;
    }

    public void setNonRenewablePowerPlantActiveStatus(Map<String, Boolean> nonRenewablePowerPlantActiveStatus) {
        this.nonRenewablePowerPlantActiveStatus = nonRenewablePowerPlantActiveStatus;
    }

    

    public List<String> getSmartBuildingNames() {
        return smartBuildingNames;
    }

    public void setSmartBuildingNames(List<String> smartBuildingNames) {
        this.smartBuildingNames = smartBuildingNames;
    }

    public Map<String, EnergyTransaction> getSmartBuildingsEnergyRequests() {
        return smartBuildingsEnergyRequests;
    }

    public void setSmartBuildingsEnergyRequests(Map<String, EnergyTransaction> smartBuildingsEnergyRequests) {
        this.smartBuildingsEnergyRequests = smartBuildingsEnergyRequests;
    }

    public void addEnergyRequest(String smartmeBuildingName, EnergyTransaction request) {
        smartBuildingsEnergyRequests.put(smartmeBuildingName, request);
    }

    public void removeEnergyRequest(String smartBuildingName) {
        smartBuildingsEnergyRequests.remove(smartBuildingName);
    }

    // public boolean consumeEnergy(double requestedEnergy) {
    // if(currentEnergy >= requestedEnergy) {
    // currentEnergy -= requestedEnergy;
    // return true;
    // }
    // return false;
    // }

    public double getExpectedConsumption() {
        return expectedConsumption;
    }

    public void setExpectedConsumption(double expectedConsumption) {
        this.expectedConsumption = expectedConsumption;
    }

    public boolean containsSmartBuildingWithoutPower(String smartBuildingName) {
        return smartBuildingsWithoutPower.containsKey(smartBuildingName);
    }

    public void addSmartBuildingWithoutPower(String smartBuildingName, EnergyTransaction energyTransaction) {
        smartBuildingsWithoutPower.put(smartBuildingName, energyTransaction);
    }

    public void removeSmartBuildingWithoutPower(String smartBuildingName) {
        smartBuildingsWithoutPower.remove(smartBuildingName);
    }

    public int getSmartBuildingWithoutPowerSize() {
        return smartBuildingsWithoutPower.size();
    }

    public void addExpectedConsumption(double energy) {
        expectedConsumption += energy;
    }

    public void addExpectedProduction(double energy) {
        expectedProduction += energy;
    }

    public double getBlackoutEnergyRequest() {
        double sum = 0;
        for (String smartBuilding : smartBuildingsWithoutPower.keySet()) {
            sum += smartBuildingsWithoutPower.get(smartBuilding).getEnergyTransactionValue();
        }
        return sum;
    }


    public String getLoadManagerName() {
        return loadManagerName;
    }

    public void setLoadManagerName(String loadManagerName) {
        this.loadManagerName = loadManagerName;
    }

    public Map<String, EnergyTransaction> getSmartBuildingsWithoutPower() {
        return smartBuildingsWithoutPower;
    }

    public boolean canSendEnergy() {
        return battery.getStateOfCharge() > 0.5;
    }

    public List<String> getRenewablePowerPlantNames() {
        return renewablePowerPlantNames;
    }

    public void setRenewablePowerPlantNames(List<String> renewablePowerPlantNames) {
        this.renewablePowerPlantNames = renewablePowerPlantNames;
    }

    public List<String> getNonRenewablePowerPlantNames() {
        return new ArrayList<String>(nonRenewablePowerPlantActiveStatus.keySet());
    }

    public void setNonRenewablePowerPlantNames(List<String> nonRenewablePowerPlantNames) {
        // this.nonRenewablePowerPlantNames = nonRenewablePowerPlantNames;
        for(String nonRenewablePowerPlantName : nonRenewablePowerPlantNames){
            nonRenewablePowerPlantActiveStatus.put(nonRenewablePowerPlantName, false);
        }
    }

    public List<Cable> getConnectedGridsCables() {
        List<Cable> connectedGridsCables = new ArrayList<Cable>();
        for (String agentName : connectedAgents.keySet()) {
            Cable cable = connectedAgents.get(agentName);
            if (cable.getTo().contains("Grid")) {
                connectedGridsCables.add(cable);
            }
        }
        return connectedGridsCables;
    }

    public void setNumberOfMessagesToReceive(int numberOfMessagesToReceive) {
        this.numberOfMessagesToReceive = numberOfMessagesToReceive;
    }

    public int getNumberOfMessagesToReceive() {
        return this.numberOfMessagesToReceive;
    }

    public void updateGridPriority(Priority priority) {
        if (priority.ordinal() < this.priority.ordinal()) {
            this.priority = priority;
        }
    }

    public Priority getPriority() {
        return priority;
    }

    public Battery getBattery() {
        return battery;
    }

    public double fillBattery(double extraEnergy) {
        return battery.fillBattery(extraEnergy);
    }

    public double getExpectedProduction() {
        return expectedProduction;
    }

    public void setExpectedProduction(double expectedProduction) {
        this.expectedProduction = expectedProduction;
    }

    public List<EnergyTransaction> getSmartBuildingsEnergyRequestsByPriority(Priority priority) {
        List<EnergyTransaction> results = new ArrayList<EnergyTransaction>(); 
        for(EnergyTransaction energyTransaction : smartBuildingsEnergyRequests.values()){
            if(energyTransaction.getPriority() == priority && energyTransaction.getTransactionType() == TransactionType.RECEIVE){
                results.add(energyTransaction);
            }
        }
        return results;
    }

    public double getBuildingRequestedEnergy() {
        double sum = 0;
        for(EnergyTransaction energyTransaction : smartBuildingsEnergyRequests.values()){
            if(energyTransaction.getTransactionType() == TransactionType.RECEIVE){
                sum += energyTransaction.getEnergyTransactionValue();
            }
        }
        return sum;
    }

    public List<EnergyTransaction> getBlackoutSmartBuildingsEnergyRequestsByPriority(Priority priority) {
        List<EnergyTransaction> results = new ArrayList<EnergyTransaction>(); 
        for(EnergyTransaction energyTransaction : smartBuildingsWithoutPower.values()){
            if(energyTransaction.getPriority() == priority){
                results.add(energyTransaction);
            }
        }
        return results;
    }

    public void setSmartBuildingsWithoutPower(Map<String, EnergyTransaction> smartBuildingsWithoutPower) {
        this.smartBuildingsWithoutPower = smartBuildingsWithoutPower;
    }

    public List<String> getGridNames() {
        return gridNames;
    }

    public void setGridNames(List<String> gridNames) {
        this.gridNames = gridNames;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
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

    public void resetValues() {
        this.expectedConsumption = 0;
        this.expectedProduction = 0;
        distributionInstructions.clear();
    }

    
    
}
