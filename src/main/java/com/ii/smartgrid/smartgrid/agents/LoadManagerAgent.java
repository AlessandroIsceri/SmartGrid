package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.CableDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.DistributeBatteryEnergyBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.DistributeExcessEnergyBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.DistributeGridEnergyBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.NonRenewablePowerPlantDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ReceiveEnergyRequestsFromGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.SendInstructionsToGridsBehaviour;
import com.ii.smartgrid.smartgrid.model.entities.LoadManager;
import com.ii.smartgrid.smartgrid.behaviours.loadmanager.ChangeNonRenewablePowerPlantsInfo;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

import jade.core.behaviours.SequentialBehaviour;

public class LoadManagerAgent extends CustomAgent{

    @Override
    public void setup() {
        String loadManagerName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.LOAD_MANAGERS_PATH, loadManagerName, LoadManager.class);
    
        this.addBehaviour(new CableDiscoveryBehaviour(this));
        this.addBehaviour(new NonRenewablePowerPlantDiscoveryBehaviour(this));
        this.addBehaviour(new LoadManagerBehaviour(this));
        log("Setup completed");
    }

    public LoadManager getLoadManager() {
        return (LoadManager) this.referencedObject;
    }

    private class LoadManagerBehaviour extends GenericTurnBehaviour{

        private LoadManagerAgent loadManagerAgent;

        public LoadManagerBehaviour(LoadManagerAgent loadManagerAgent){
            super(loadManagerAgent);
            this.loadManagerAgent = loadManagerAgent;
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            // 1. Receives energy requests/supplies from the grids
            // 2. Tries to satisfy the greatest number of energy requests from the grids ordered by priority using the energy provided by other grids
            // 3. Tries to satisfy the greatest number of remaining energy requests from the grids ordered by priority using the energy contained in the batteries
            // 4. Compute the best routing path for sending excess energy into grids with a battery
            // 5. Handle the activation or deactivation of NonRenewablePowerPlants based on energy availability
            // 6. Send the routing instructions to the grids

            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyRequestsFromGridBehaviour(loadManagerAgent));
            sequentialTurnBehaviour.addSubBehaviour(new DistributeGridEnergyBehaviour(loadManagerAgent));
            sequentialTurnBehaviour.addSubBehaviour(new DistributeBatteryEnergyBehaviour(loadManagerAgent));
            sequentialTurnBehaviour.addSubBehaviour(new DistributeExcessEnergyBehaviour(loadManagerAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ChangeNonRenewablePowerPlantsInfo(loadManagerAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendInstructionsToGridsBehaviour(loadManagerAgent));
        }
    }
}
