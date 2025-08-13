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
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendNonRenewableEnergyToGridBehaviour extends CustomOneShotBehaviour{

	public SendNonRenewableEnergyToGridBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent) {
		super(nonRenewablePowerPlantAgent);
	}
	
	@Override
	public void action() {

        NonRenewablePowerPlant nonRenewablePowerPlant = ((NonRenewablePowerPlantAgent) myAgent).getNonRenewablePowerPlant();
        
        double givenEnergy = nonRenewablePowerPlant.getHourlyProduction() * TimeUtils.getTurnDurationHours();;

        Map<String, Object> content = new HashMap<String, Object>();
        String gridName = nonRenewablePowerPlant.getGridName();
        
        Cable cable = nonRenewablePowerPlant.getCable(gridName);
        content.put(MessageUtil.GIVEN_ENERGY, cable.computeTransmittedPower(givenEnergy));
        ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, gridName, content);
	}

}
