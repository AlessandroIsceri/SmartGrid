package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ReceiveGridRequestsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ReceiveRenewablePowerPlantInformBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.SendGridAnswer;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.SendPowerPlantRequestsBehaviour;

import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class LoadManager extends CustomAgent{

	private List<String> gridNames;
    private List<String> renewablePowerPlantNames;
    private List<String> nonRenewablePowerPlantNames;
    private LinkedHashMap<String, Double> gridRequestedEnergy;
    private double expectedConsumption;

    @Override
    public void setup() {
        Object[] args = this.getArguments();
		gridNames = new ArrayList<String>();
		renewablePowerPlantNames = new ArrayList<String>();
        nonRenewablePowerPlantNames = new ArrayList<String>();
        gridRequestedEnergy = new LinkedHashMap<>();
		
        boolean readingGrids = true;
		
		for(int i = 0; i < args.length; i++) {
			String curArg = ((String) args[i]);
			if(curArg.equals("**")) {
				readingGrids = false;
			}else {
				if(readingGrids){
					gridNames.add(curArg);
				} else {
                    boolean inserted = false;
                    for(String ppType: RenewablePowerPlant.TYPES){
                        if(curArg.contains(ppType)){
                            renewablePowerPlantNames.add(curArg);
                            inserted = true;
                            break;
                        }
                    }
                    for(String ppType: NonRenewablePowerPlant.TYPES){
                        if(curArg.contains(ppType)){
                            nonRenewablePowerPlantNames.add(curArg);
                            inserted = true;
                            break;
                        }
                    }
                    if(!inserted){
                        log("There is an error in LoadManager setup, pp " + curArg + " is not a valid name containing the pp type.");
                    }
				}
			}
		}
        addBehaviour(new LoadManagerBehaviour(this));
        log("Setup completed");
    }

    private class LoadManagerBehaviour extends GenericTurnBehaviour{

        public LoadManagerBehaviour(LoadManager loadManager){
            super(loadManager);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            // riceve le richieste dalle grid
            // manda richieste ai powerplant **dove ER > ENR** e riceve risposta dai powerplant
            // manda risposte alle grid
        
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveGridRequestsBehaviour((LoadManager) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveRenewablePowerPlantInformBehaviour((LoadManager) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendPowerPlantRequestsBehaviour((LoadManager) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendGridAnswer((LoadManager) myAgent));
            ((LoadManager) myAgent).setExpectedConsumption(0);
        }

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

    public LinkedHashMap<String, Double> getGridRequestedEnergy() {
        return gridRequestedEnergy;
    }
}
