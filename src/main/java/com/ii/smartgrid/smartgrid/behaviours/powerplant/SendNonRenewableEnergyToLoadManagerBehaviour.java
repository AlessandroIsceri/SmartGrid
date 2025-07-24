package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.nio.channels.AcceptPendingException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.NonRenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendNonRenewableEnergyToLoadManagerBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

	public SendNonRenewableEnergyToLoadManagerBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent) {
		super(nonRenewablePowerPlantAgent);
	}
	
	@Override
	public void action() {
        
        NonRenewablePowerPlant nonRenewablePowerPlant = ((NonRenewablePowerPlantAgent) myAgent).getNonRenewablePowerPlant();

        double requestedEnergy = nonRenewablePowerPlant.getRequestedEnergy();
        double hourlyProduction = nonRenewablePowerPlant.getHourlyProduction();
        double turnProduction = hourlyProduction * TimeUtils.getTurnDurationHours();
        
        double givenEnergy = 0;
        if(requestedEnergy < turnProduction){
            givenEnergy = requestedEnergy;
        } else {
            givenEnergy = turnProduction;
        }
        
        Map<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.GIVEN_ENERGY, givenEnergy);
        String loadManagerName = nonRenewablePowerPlant.getLoadManagerName();
        ((CustomAgent) myAgent).createAndSend(ACLMessage.AGREE, loadManagerName, content);
	}

}
