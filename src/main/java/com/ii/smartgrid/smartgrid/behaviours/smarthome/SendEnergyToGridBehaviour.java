package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.SmartHome;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyToGridBehaviour extends OneShotBehaviour{

    public SendEnergyToGridBehaviour(SmartHome smartHome){
        super(smartHome);
    }

    @Override
    public void action() {
        // Store extra energy into home's battery (if available)
        String gridName = ((SmartHome) myAgent).getGridName();
        double expectedConsumption = ((SmartHome) myAgent).getExpectedConsumption();
        double expectedProduction = ((SmartHome) myAgent).getExpectedProduction();
        Battery battery = ((SmartHome) myAgent).getBattery();
        Map<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.OPERATION, MessageUtil.RELEASE);
        
        if(battery != null){
            double extraEnergy = battery.fillBattery(expectedProduction - expectedConsumption);
            // Release extra energy into the grid
            content.put(MessageUtil.RELEASED_ENERGY, extraEnergy);
        } else {
            // Battery not available -> release extra energy into the grid
            content.put(MessageUtil.RELEASED_ENERGY, (expectedProduction - expectedConsumption));
        }
        ((SmartHome) myAgent).createAndSend(ACLMessage.INFORM, gridName, content, "release-" + myAgent.getLocalName());
    }
    
}