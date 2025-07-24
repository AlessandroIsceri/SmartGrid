package com.ii.smartgrid.smartgrid.behaviours.smarthome;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyRequestToGridBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendEnergyRequestToGridBehaviour(SmartHomeAgent smartHomeAgent){
        super(smartHomeAgent);
    }

    @Override
    public void action() {
        SmartHome smartHome = ((SmartHomeAgent) myAgent).getSmartHome(); 
        ((CustomAgent) myAgent).log("SendEnergyRequestToGridBehaviour STARTED", BEHAVIOUR_NAME);
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //può rilasciare energia se ne ha troppa e non gli serve
		double expectedConsumption = smartHome.getExpectedConsumption();
        double availableEnergy = smartHome.getAvailableEnergy();
	
        // Request energy from the grid
        Map<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.OPERATION, MessageUtil.CONSUME);
        content.put(MessageUtil.REQUESTED_ENERGY, (expectedConsumption - availableEnergy));

        String gridName = smartHome.getGridName();
        ((CustomAgent) myAgent).createAndSend(ACLMessage.REQUEST, gridName, content);
        ((CustomAgent) myAgent).log("SendEnergyRequestToGridBehaviour FINISHED", BEHAVIOUR_NAME);
	} 
}