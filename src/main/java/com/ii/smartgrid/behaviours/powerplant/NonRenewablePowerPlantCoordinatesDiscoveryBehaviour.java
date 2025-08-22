package com.ii.smartgrid.behaviours.powerplant;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.agents.NonRenewablePowerPlantAgent;
import com.ii.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.model.entities.NonRenewablePowerPlant;
import com.ii.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.utils.TimeUtils;

import jade.lang.acl.ACLMessage;

public class NonRenewablePowerPlantCoordinatesDiscoveryBehaviour extends CoordinatesDiscoveryBehaviour{

    private NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent;

    public NonRenewablePowerPlantCoordinatesDiscoveryBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent) {
        super(nonRenewablePowerPlantAgent);
        this.nonRenewablePowerPlantAgent = nonRenewablePowerPlantAgent;
    }

    @Override
    protected void sendInformationToLoadManager(){
        NonRenewablePowerPlant nonRenewablePowerPlant = nonRenewablePowerPlantAgent.getNonRenewablePowerPlant();
        Map<String, Object> content = new HashMap<>();
        content.put(MessageUtil.ON, nonRenewablePowerPlant.isOn());
        content.put(MessageUtil.MAX_TURN_PRODUCTION, nonRenewablePowerPlant.getMaxHourlyProduction() * TimeUtils.getTurnDurationHours());
        String loadManagerName = nonRenewablePowerPlant.getLoadManagerName();
        customAgent.createAndSend(ACLMessage.INFORM, loadManagerName, content);
    }
}
