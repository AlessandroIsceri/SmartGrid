package com.ii.smartgrid.smartgrid.behaviours.smarthome;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.EnergyProducer;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyRequestToGridBehaviour extends OneShotBehaviour{

    public SendEnergyRequestToGridBehaviour(SmartHome smartHome){
        super(smartHome);
    }

    @Override
    public void action() {
        ((SmartHome) myAgent).log("SendEnergyRequestToGridBehaviour STARTED");
        //richiede energia se serve (non ne ha abbastanza, attaccandosi alla rete)
        //può rilasciare energia se ne ha troppa e non gli serve
		double expectedConsumption = ((SmartHome) myAgent).getExpectedConsumption();
        double availableEnergy = ((SmartHome) myAgent).getAvailableEnergy();
	
        // Request energy from the grid
        Map<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.OPERATION, MessageUtil.CONSUME);
        content.put(MessageUtil.REQUESTED_ENERGY, (expectedConsumption - availableEnergy));

        String gridName = ((SmartHome) myAgent).getGridName();
        ((SmartHome) myAgent).createAndSend(ACLMessage.REQUEST, gridName, content);
        ((SmartHome) myAgent).log("SendEnergyRequestToGridBehaviour FINISHED");
	} 
}