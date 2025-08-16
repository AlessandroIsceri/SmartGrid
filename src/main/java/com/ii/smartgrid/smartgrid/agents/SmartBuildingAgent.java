package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smartbuilding.CheckSmartBuildingMessagesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smartbuilding.ReceiveEnergyFromGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smartbuilding.SendEnergyRequestToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smartbuilding.WaitForRestoreBehaviour;
import com.ii.smartgrid.smartgrid.model.SmartBuilding;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

import jade.core.behaviours.SequentialBehaviour;

public class SmartBuildingAgent extends CustomAgent{
	
    public enum SmartBuildingStatus {GAINING_ENERGY, LOSING_ENERGY, BLACKOUT}
	private SmartBuildingStatus buildingStatus = SmartBuildingStatus.LOSING_ENERGY;
    
    

	@Override
    public void setup() {    
        
        String smartBuildingName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.SMART_BUILDINGS_PATH, smartBuildingName, SmartBuilding.class);
            
        SmartBuilding smartBuilding = getSmartBuilding();

        smartBuilding.addConnectedAgentName(smartBuilding.getGridName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new SmartBuildingTurnBehaviour(this));
        this.addBehaviour(new CheckSmartBuildingMessagesBehaviour(this));
    }

    public SmartBuilding getSmartBuilding(){
        return (SmartBuilding) this.referencedObject;
    }

    private class SmartBuildingTurnBehaviour extends GenericTurnBehaviour{

        private SmartBuildingAgent smartBuildingAgent;

        private SmartBuildingTurnBehaviour(SmartBuildingAgent smartBuildingAgent){
            super(smartBuildingAgent);
            this.smartBuildingAgent = smartBuildingAgent;
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            SmartBuilding smartBuilding = smartBuildingAgent.getSmartBuilding();
            // Compute the expected production and consumption based on the current routine
            smartBuilding.followRoutine(curTurn, curWeather, buildingStatus);
            if(buildingStatus == SmartBuildingStatus.BLACKOUT){
                log("Blackout");
                // If a SmartBuilding is in blackout status, it waits for a restore message from the grid
                sequentialTurnBehaviour.addSubBehaviour(new WaitForRestoreBehaviour(smartBuildingAgent));
            } else {
                double availableEnergy = smartBuilding.getExpectedProduction();
				double expectedConsumption = smartBuilding.getExpectedConsumption();
                
                // Send a request to the grid containing the amount of energy that the building needs/releases
                sequentialTurnBehaviour.addSubBehaviour(new SendEnergyRequestToGridBehaviour(smartBuildingAgent));
                if(availableEnergy >= expectedConsumption){ 
                    buildingStatus = SmartBuildingStatus.GAINING_ENERGY;
                }else{
                    buildingStatus = SmartBuildingStatus.LOSING_ENERGY;
                    // If the Building has requested energy, wait for the grid's response
                    sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromGridBehaviour(smartBuildingAgent));
                }
            }
        }
    }

    public SmartBuildingStatus getBuildingStatus() {
        return buildingStatus;
    }

    public void setBuildingStatus(SmartBuildingStatus buildingStatus) {
        this.buildingStatus = buildingStatus;
    }

}
