package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.CableDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.DistributeBatteryEnergyBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.DistributeExcessEnergyBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.FindOptimalDistributionStrategyBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.NonRenewablePowerPlantDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ReceiveEnergyRequestsFromGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.SendInstructionToGridsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ChangeNonRenewablePowerPlantsInfo;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

import jade.core.behaviours.SequentialBehaviour;

public class LoadManagerAgent extends CustomAgent{

	// private LoadManager loadManager;

    @Override
    public void setup() {
        String loadManagerName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.LOAD_MANAGERS_PATH, loadManagerName, LoadManager.class);
        
        LoadManager loadManager = this.getLoadManager();
        // this.referencedObject.addConnectedAgentNames(loadManager.getGridNames());
        // int numberOfGrids = (int) this.getArguments()[0];
        // int numberOfNonRenewablePowerPlants = (int) this.getArguments()[1];
        
    
        // this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new CableDiscoveryBehaviour(this));
        this.addBehaviour(new NonRenewablePowerPlantDiscoveryBehaviour(this));
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
            // pensa all'instradamento dell'energia
            // se necessario (se il totale è < 0) -> prova con le batterie available
            // se necessario (se la percentuale di una o più batterie è < 20%) -> accende pp non rinnovabili; altrimenti, se sono tutte > 80% li spegne
            // comunicare l'instradamento ad ogni grid e che pp non rinnovabili sono attive x il turno dopo
        
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyRequestsFromGridBehaviour((LoadManagerAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new FindOptimalDistributionStrategyBehaviour((LoadManagerAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new DistributeBatteryEnergyBehaviour((LoadManagerAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new DistributeExcessEnergyBehaviour((LoadManagerAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ChangeNonRenewablePowerPlantsInfo((LoadManagerAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendInstructionToGridsBehaviour((LoadManagerAgent) myAgent));
        }
    }
}
