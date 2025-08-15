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
        
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //puo' rilasciare energia se ne ha troppa e non gli serve
        //spegnere/accendere gli elettrodomestici in base alla routine decisa dall'owner
        
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
            smartBuilding.followRoutine(curTurn, curWeather, buildingStatus);
            if(buildingStatus == SmartBuildingStatus.BLACKOUT){
                log("else (status blackout)");
                sequentialTurnBehaviour.addSubBehaviour(new WaitForRestoreBehaviour(smartBuildingAgent));
            } else {
                // LOSING or GAINING
                double availableEnergy = smartBuilding.getExpectedProduction();
				double expectedConsumption = smartBuilding.getExpectedConsumption();

                sequentialTurnBehaviour.addSubBehaviour(new SendEnergyRequestToGridBehaviour(smartBuildingAgent));
                if(availableEnergy >= expectedConsumption){ 
                    buildingStatus = SmartBuildingStatus.GAINING_ENERGY;
                }else{
                    buildingStatus = SmartBuildingStatus.LOSING_ENERGY;
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
