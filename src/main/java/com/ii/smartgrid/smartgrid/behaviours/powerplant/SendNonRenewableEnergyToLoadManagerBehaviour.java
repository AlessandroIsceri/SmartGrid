package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.nio.channels.AcceptPendingException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.agents.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.agents.PowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendNonRenewableEnergyToLoadManagerBehaviour extends OneShotBehaviour{

    
	private NonRenewablePowerPlant powerPlant;
	
	public SendNonRenewableEnergyToLoadManagerBehaviour(NonRenewablePowerPlant nonRenewablePowerPlant) {
		this.powerPlant = powerPlant;
	}
	
	@Override
	public void action() {
        
        double requestedEnergy = ((NonRenewablePowerPlant) myAgent).getRequestedEnergy();
        double hourlyProduction = ((NonRenewablePowerPlant) myAgent).getHourlyProduction();
        double turnProduction = hourlyProduction / 60 * TimeUtils.getTurnDuration();
        
        double givenEnergy = 0;
        if(requestedEnergy < turnProduction){
            givenEnergy = requestedEnergy;
        } else {
            givenEnergy = turnProduction;
        }
        
        Map<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.GIVEN_ENERGY, givenEnergy);
        String loadManagerName = ((NonRenewablePowerPlant) myAgent).getLoadManagerName();
        ((NonRenewablePowerPlant) myAgent).createAndSend(ACLMessage.AGREE, loadManagerName, content);
	}

}
