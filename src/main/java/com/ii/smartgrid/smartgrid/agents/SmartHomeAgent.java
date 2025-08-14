package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.CheckSmartHomeMessagesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.ReceiveEnergyFromGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.SendEnergyRequestToGridBehaviour;
// import com.ii.smartgrid.smartgrid.behaviours.smarthome.SendEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.WaitForRestoreBehaviour;
import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

import jade.core.behaviours.SequentialBehaviour;

public class SmartHomeAgent extends CustomAgent{
	
    public enum SmartHomeStatus {GAINING_ENERGY, LOSING_ENERGY, BLACKOUT};
	private SmartHomeStatus homeStatus = SmartHomeStatus.LOSING_ENERGY;
    
    

	@Override
    public void setup() {    
        
        String smartHomeName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.SMART_HOMES_PATH, smartHomeName, SmartHome.class);
        
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //puo' rilasciare energia se ne ha troppa e non gli serve
        //spegnere/accendere gli elettrodomestici in base alla routine decisa dall'owner
        
        SmartHome smartHome = getSmartHome();

        smartHome.addConnectedAgentName(smartHome.getGridName());
        
       
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new SmartHomeTurnBehaviour(this));
        this.addBehaviour(new CheckSmartHomeMessagesBehaviour(this));
    }

    public SmartHome getSmartHome(){
        return (SmartHome) this.referencedObject;
    }

    private class SmartHomeTurnBehaviour extends GenericTurnBehaviour{

        private SmartHomeAgent smartHomeAgent;

        private SmartHomeTurnBehaviour(SmartHomeAgent smartHomeAgent){
            super(smartHomeAgent);
            this.smartHomeAgent = smartHomeAgent;
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            SmartHome smartHome = smartHomeAgent.getSmartHome();
            smartHome.followRoutine(curTurn, curWeather, homeStatus);
            if(homeStatus == SmartHomeStatus.BLACKOUT){
                log("else (status blackout)");
                sequentialTurnBehaviour.addSubBehaviour(new WaitForRestoreBehaviour(smartHomeAgent));
            } else {
                // LOSING or GAINING
                double availableEnergy = smartHome.getExpectedProduction();
				double expectedConsumption = smartHome.getExpectedConsumption();

                sequentialTurnBehaviour.addSubBehaviour(new SendEnergyRequestToGridBehaviour(smartHomeAgent));
                if(availableEnergy >= expectedConsumption){ 
                    homeStatus = SmartHomeStatus.GAINING_ENERGY;
                }else{
                    homeStatus = SmartHomeStatus.LOSING_ENERGY;
                    sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromGridBehaviour(smartHomeAgent));
                }
            }
        }
    }

    public SmartHomeStatus getHomeStatus() {
        return homeStatus;
    }

    public void setHomeStatus(SmartHomeStatus homeStatus) {
        this.homeStatus = homeStatus;
    }

}
