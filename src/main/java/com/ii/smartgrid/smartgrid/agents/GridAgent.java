package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ReceiveEnergyFromLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ReceiveEnergyRequestsFromSmartHomesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.SendEnergyRequestToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.SendEnergyToSmartHomesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.SendRestoreMessagesToSmartHomesBehaviour;
import com.ii.smartgrid.smartgrid.model.DieselPowerPlant;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class GridAgent extends CustomAgent{

	@Override
    public void setup() {
        String gridName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.GRIDS_PATH, gridName, Grid.class);

        Grid grid = this.getGrid();
        this.referencedObject.addConnectedAgentNames(grid.getSmartHomeNames());
        this.referencedObject.addConnectedAgentName(grid.getLoadManagerName());

        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new GridBehaviour(this));
        this.log("Setup completed");        
	}

    public Grid getGrid(){
        return (Grid) this.referencedObject;
    }

    private class GridBehaviour extends GenericTurnBehaviour{

        public GridBehaviour(GridAgent gridAgent) {
            super(gridAgent);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            //ricevi le richieste (o se in blackout o meno una casa)
            //chiedi energia al load manager
            //aspetta risposta load manager x sapere quanta energia arriverà
            //invia le risposte alle case
            //aspetta i messaggi di blackout
            //restora le case se riesci

			//SendXYZ -> inviare messaggi 
			//ReceiveXYZ -> ricevere risposte ai messaggi

            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyRequestsFromSmartHomesBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendEnergyRequestToLoadManagerBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromLoadManagerBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendEnergyToSmartHomesBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendRestoreMessagesToSmartHomesBehaviour((GridAgent) myAgent));
            Grid grid = ((GridAgent) myAgent).getGrid();
            grid.setExpectedConsumption(grid.getBlackoutEnergyRequest());
        }

    }

}
