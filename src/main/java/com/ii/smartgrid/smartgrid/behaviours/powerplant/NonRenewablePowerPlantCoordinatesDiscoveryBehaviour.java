package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.agents.NonRenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.lang.acl.ACLMessage;

public class NonRenewablePowerPlantCoordinatesDiscoveryBehaviour extends CoordinatesDiscoveryBehaviour{
    
    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();


    public NonRenewablePowerPlantCoordinatesDiscoveryBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent) {
        super(nonRenewablePowerPlantAgent);
    }

    @Override
    protected void sendInformationToLoadManager(){
        NonRenewablePowerPlant nonRenewablePowerPlant = ((NonRenewablePowerPlantAgent) myAgent).getNonRenewablePowerPlant();
        Map<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.ON, nonRenewablePowerPlant.isOn());
        content.put(MessageUtil.MAX_TURN_PRODUCTION, nonRenewablePowerPlant.getHourlyProduction() * TimeUtils.getTurnDurationHours());
        String loadManagerName = nonRenewablePowerPlant.getLoadManagerName();
        ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, loadManagerName, content, "nonRenewablePowerPlantInfo");
    }
}
