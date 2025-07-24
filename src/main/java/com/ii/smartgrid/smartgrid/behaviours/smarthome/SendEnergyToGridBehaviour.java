package com.ii.smartgrid.smartgrid.behaviours.smarthome;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.model.SmartHome;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyToGridBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendEnergyToGridBehaviour(SmartHomeAgent smartHomeAgent){
        super(smartHomeAgent);
    }

    @Override
    public void action() {
        // Store extra energy into home's battery (if available)
        SmartHome smartHome = ((SmartHomeAgent) myAgent).getSmartHome();
        String gridName = smartHome.getGridName();
        double expectedConsumption = smartHome.getExpectedConsumption();
        double expectedProduction = smartHome.getExpectedProduction();
        Battery battery = smartHome.getBattery();
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
        ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, gridName, content, "release-" + myAgent.getLocalName());
    }
    
}