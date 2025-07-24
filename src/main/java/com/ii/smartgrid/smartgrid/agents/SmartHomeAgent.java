package com.ii.smartgrid.smartgrid.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.PowerPlant.PPStatus;
import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.GenericTurnBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.CheckSmartHomeMessagesBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.ReceiveEnergyFromGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.SendEnergyRequestToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.SendEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.WaitForRestoreBehaviour;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.Owner;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.model.SmartHome.SmartHomeStatus;
import com.ii.smartgrid.smartgrid.model.Task;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;

public class SmartHomeAgent extends CustomAgent{
	// private SmartHome smartHome;
	
	@Override
    public void setup() {    
        
        String smartHomeName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.SMART_HOMES_PATH, smartHomeName, SmartHome.class);
        
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //puo' rilasciare energia se ne ha troppa e non gli serve
        //spegnere/accendere gli elettrodomestici in base alla routine decisa dall'owner
        
        this.referencedObject.addConnectedAgentName(this.getSmartHome().getGridName());
        
       
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new SmartHomeTurnBehaviour(this));
        this.addBehaviour(new CheckSmartHomeMessagesBehaviour(this));
    }

    public SmartHome getSmartHome(){
        return (SmartHome) this.referencedObject;
    }

    private class SmartHomeTurnBehaviour extends GenericTurnBehaviour{

        private SmartHomeTurnBehaviour(SmartHomeAgent smartHomeAgent){
            super(smartHomeAgent);
        }

        @Override
        protected void executeTurn(SequentialBehaviour sequentialTurnBehaviour) {
            SmartHome smartHome = ((SmartHomeAgent) myAgent).getSmartHome();
            log("home status: " + smartHome.getStatus());
            if(smartHome.getStatus() == SmartHomeStatus.WORKING){
                
				smartHome.followRoutine(curTurn, curWeather);
			
                double availableEnergy = smartHome.getAvailableEnergy();
				double expectedConsumption = smartHome.getExpectedConsumption();

                if(expectedConsumption > availableEnergy){
                    sequentialTurnBehaviour.addSubBehaviour(new SendEnergyRequestToGridBehaviour((SmartHomeAgent) myAgent));
                    sequentialTurnBehaviour.addSubBehaviour(new ReceiveEnergyFromGridBehaviour((SmartHomeAgent) myAgent));
                }else{
                    sequentialTurnBehaviour.addSubBehaviour(new SendEnergyToGridBehaviour((SmartHomeAgent) myAgent));
                }
                
            }
            else if(smartHome.getStatus() == SmartHomeStatus.BLACKOUT){
				log("else (status blackout)");
                sequentialTurnBehaviour.addSubBehaviour(new WaitForRestoreBehaviour((SmartHomeAgent) myAgent));
            }
        }
    }
}
