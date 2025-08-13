package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.FollowRoutingInstructionsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.GridCoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.HandleExtraEnergyBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.InitRoutingInstructionsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ReceiveEnergyFromNonRenewablePowerPlantsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ReceiveEnergyFromRenewablePowerPlantsBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ReceiveEnergyRequestsFromSmartHomesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.ReceiveRoutingInstructionsFromLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.SendEnergyRequestToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.grid.SendEnergyToSmartHomesBehaviour;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

import jade.core.behaviours.SequentialBehaviour;

public class GridAgent extends CustomAgent{

    public enum GridStatus {RECEIVE, SEND};
	private GridStatus gridStatus = null;

	@Override
    public void setup() {
        String gridName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.GRIDS_PATH, gridName, Grid.class);

        Grid grid = this.getGrid();
        //this.referencedObject.addConnectedAgentName(grid.getLoadManagerName());

        this.referencedObject.addConnectedAgentNames(grid.getSmartHomeNames());
        this.referencedObject.addConnectedAgentNames(grid.getNonRenewablePowerPlantNames());
        this.referencedObject.addConnectedAgentNames(grid.getRenewablePowerPlantNames());
        this.referencedObject.addConnectedAgentNames(grid.getGridNames());

        this.addBehaviour(new GridCoordinatesDiscoveryBehaviour(this));
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
            //riceve energie da powerplant rinnovabili
            //se ci sono non rinnovabili attive, dobbiamo aspettare la loro energia
            //chiedi energia al load manager
            //aspetta risposta load manager x sapere quanta energia arriverà
            //invia le risposte alle case
            //aspetta i messaggi di blackout
            //restora le case se riesci

			//SendXYZ -> inviare messaggi 
			//ReceiveXYZ -> ricevere risposte ai messaggi

            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyRequestsFromSmartHomesBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromRenewablePowerPlantsBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromNonRenewablePowerPlantsBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendEnergyRequestToLoadManagerBehaviour((GridAgent) myAgent));

            sequentialTurnBehaviour.addSubBehaviour(new ReceiveRoutingInstructionsFromLoadManagerBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new InitRoutingInstructionsBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new FollowRoutingInstructionsBehaviour((GridAgent) myAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendEnergyToSmartHomesBehaviour((GridAgent) myAgent));
            // sequentialTurnBehaviour.addSubBehaviour(new SendRestoreMessagesToSmartHomesBehaviour((GridAgent) myAgent));
            
            sequentialTurnBehaviour.addSubBehaviour(new HandleExtraEnergyBehaviour((GridAgent)myAgent));
        }

    }

    public GridStatus getGridStatus() {
        return gridStatus;
    }

    public void setGridStatus(GridStatus gridStatus) {
        this.gridStatus = gridStatus;
    }

}
