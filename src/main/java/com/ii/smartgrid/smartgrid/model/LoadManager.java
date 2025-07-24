package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ReceiveEnergyFromRenewablePowerPlantsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ReceiveEnergyRequestsFromGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.SendEnergyRequestsToNonRenewablePowerPlantsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.SendEnergyToGridsBehaviour;

import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class LoadManager extends CustomObject{

	private List<String> gridNames;
    private List<String> renewablePowerPlantNames;
    private List<String> nonRenewablePowerPlantNames;
    private Map<String, Double> gridRequestedEnergy;
    private double expectedConsumption;
    private Battery battery;


    public LoadManager(){
        super();
        gridRequestedEnergy = new HashMap<String, Double>();
        expectedConsumption = 0;
    }

    public List<String> getGridNames() {
        return gridNames;
    }

    public void setGridNames(List<String> gridNames) {
        this.gridNames = gridNames;
    }

    public List<String> getRenewablePowerPlantNames() {
        return renewablePowerPlantNames;
    }

    public void setRenewablePowerPlantNames(List<String> renewablePowerPlantNames) {
        this.renewablePowerPlantNames = renewablePowerPlantNames;
    }

    public double getExpectedConsumption() {
        return expectedConsumption;
    }

    public void setExpectedConsumption(double expectedConsumption) {
        this.expectedConsumption = expectedConsumption;
    }

    public void addExpectedConsumption(double energy) {
        expectedConsumption += energy;
    }

    public void removeExpectedConsumption(double energy) {
        expectedConsumption -= energy;
    }

    public List<String> getNonRenewablePowerPlantNames() {
        return nonRenewablePowerPlantNames;
    }

    public void addGridRequestedEnergy(String sender, double energy) {
        gridRequestedEnergy.put(sender, energy);
    }

    public Map<String, Double> getGridRequestedEnergy() {
        return gridRequestedEnergy;
    }

    public Battery getBattery() {
        return battery;
    }

    
}
