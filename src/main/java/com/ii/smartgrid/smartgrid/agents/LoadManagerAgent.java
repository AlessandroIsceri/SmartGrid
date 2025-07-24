package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ReceiveEnergyFromRenewablePowerPlantsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ReceiveEnergyRequestsFromGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.SendEnergyRequestsToNonRenewablePowerPlantsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.SendEnergyToGridsBehaviour;
import com.ii.smartgrid.smartgrid.model.HydroPowerPlant;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class LoadManagerAgent extends CustomAgent{

	// private LoadManager loadManager;

    @Override
    public void setup() {
        String loadManagerName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.LOAD_MANAGERS_PATH, loadManagerName, LoadManager.class);
        
        LoadManager loadManager = this.getLoadManager();
        this.referencedObject.addConnectedAgentNames(loadManager.getGridNames());
        this.referencedObject.addConnectedAgentNames(loadManager.getNonRenewablePowerPlantNames());
        this.referencedObject.addConnectedAgentNames(loadManager.getRenewablePowerPlantNames());
    
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new LoadManagerBehaviour(this));
        log("Setup completed");
    }

    public LoadManager getLoadManager() {
        return (LoadManager) this.referencedObject;
    }

    private class LoadManagerBehaviour extends GenericTurnBehaviour{

        public LoadManagerBehaviour(LoadManagerAgent loadManagerAgent){
            super(loadManagerAgent);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            // riceve le richieste dalle grid
            // ricevi l'energia rinnovabile prodotta
            // manda richieste ai powerplant **dove ER > ENR** e riceve risposta dai powerplant
            // manda risposte alle grid
        
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyRequestsFromGridBehaviour((LoadManagerAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromRenewablePowerPlantsBehaviour((LoadManagerAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendEnergyRequestsToNonRenewablePowerPlantsBehaviour((LoadManagerAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendEnergyToGridsBehaviour((LoadManagerAgent) myAgent));
            LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
            loadManager.setExpectedConsumption(0);
        }

    }

    
}
