package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.behaviours.grid.FollowRoutingInstructionsBehaviour;
import com.ii.smartgrid.behaviours.grid.GridCoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.behaviours.grid.HandleExtraEnergyBehaviour;
import com.ii.smartgrid.behaviours.grid.InitRoutingInstructionsBehaviour;
import com.ii.smartgrid.behaviours.grid.ReceiveEnergyFromNonRenewablePowerPlantsBehaviour;
import com.ii.smartgrid.behaviours.grid.ReceiveEnergyFromRenewablePowerPlantsBehaviour;
import com.ii.smartgrid.behaviours.grid.ReceiveEnergyRequestsFromSmartBuildingsBehaviour;
import com.ii.smartgrid.behaviours.grid.ReceiveRoutingInstructionsFromLoadManagerBehaviour;
import com.ii.smartgrid.behaviours.grid.SendEnergyRequestToLoadManagerBehaviour;
import com.ii.smartgrid.behaviours.grid.SendEnergyToSmartBuildingsBehaviour;
import com.ii.smartgrid.model.entities.Grid;
import com.ii.smartgrid.utils.JsonUtil;

import jade.core.behaviours.SequentialBehaviour;

public class GridAgent extends CustomAgent{

    public enum GridStatus {RECEIVE, SEND}
	private GridStatus gridStatus = null;

	@Override
    public void setup() {
        String gridName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.GRIDS_PATH, gridName, Grid.class);

        Grid grid = this.getGrid();

        grid.addConnectedAgentNames(grid.getSmartBuildingNames());
        grid.addConnectedAgentNames(grid.getNonRenewablePowerPlantNames());
        grid.addConnectedAgentNames(grid.getRenewablePowerPlantNames());
        grid.addConnectedAgentNames(grid.getGridNames());
        grid.turnOnAllNonRenewablePowerPlants();
        this.addBehaviour(new GridCoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new GridBehaviour(this));
        this.log("Setup completed");        
	}

    public Grid getGrid(){
        return (Grid) this.referencedObject;
    }

    private class GridBehaviour extends GenericTurnBehaviour{

        private GridAgent gridAgent;

        public GridBehaviour(GridAgent gridAgent) {
            super(gridAgent);
            this.gridAgent = gridAgent;
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            // 1. Receives energy requests from SmartBuildings (a SmartBuilding can request or release energy to grid) 
            // 2. Receives energy produced by Renewable Power plants
            // 3. Receives energy produced by Non-Renewable Power plants (if active)
            // 4. Sends the energy requested/available to the LoadManager in order to receive routing instructions
            // 5. Receive the Distribution Instructions from LoadManager
            // 6. Forward routing messages following the protocol given by LoadManager to other grids
            // 7. Follow the instructions given by the load manager for handling the energy
            // 8. Send the energy to SmartBuilding (Following a priority protocol)
            // 9. Handle the excess energy (by storing the extra energy into its battery)
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyRequestsFromSmartBuildingsBehaviour(gridAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromRenewablePowerPlantsBehaviour(gridAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromNonRenewablePowerPlantsBehaviour(gridAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendEnergyRequestToLoadManagerBehaviour(gridAgent));
            sequentialTurnBehaviour.addSubBehaviour(new ReceiveRoutingInstructionsFromLoadManagerBehaviour(gridAgent));
            sequentialTurnBehaviour.addSubBehaviour(new InitRoutingInstructionsBehaviour(gridAgent));
            sequentialTurnBehaviour.addSubBehaviour(new FollowRoutingInstructionsBehaviour(gridAgent));
            sequentialTurnBehaviour.addSubBehaviour(new SendEnergyToSmartBuildingsBehaviour(gridAgent));
            sequentialTurnBehaviour.addSubBehaviour(new HandleExtraEnergyBehaviour(gridAgent));
        }

    }

    public GridStatus getGridStatus() {
        return gridStatus;
    }

    public void setGridStatus(GridStatus gridStatus) {
        this.gridStatus = gridStatus;
    }

}
