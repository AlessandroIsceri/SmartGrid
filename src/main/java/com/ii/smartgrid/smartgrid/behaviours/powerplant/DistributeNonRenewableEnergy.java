package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.nio.channels.AcceptPendingException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

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

public class DistributeNonRenewableEnergy extends Behaviour{

    private boolean finished = false;
	private NonRenewablePowerPlant powerPlant;
	
	public DistributeNonRenewableEnergy(NonRenewablePowerPlant nonRenewablePowerPlant) {
		this.powerPlant = powerPlant;
	}
	
	@Override
	public void action() {
        //((NonRenewablePowerPlant) myAgent).log("Action Started");
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage receivedMessage = myAgent.receive(mt);
        if(receivedMessage != null){
            //((NonRenewablePowerPlant) myAgent).log("Received a message");
			ObjectMapper objectMapper = new ObjectMapper();
			try {
                String receivedContent = receivedMessage.getContent();
				TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
				HashMap<String, Object> jsonObject;
				jsonObject = objectMapper.readValue(receivedContent, typeRef);
				double requestedEnergy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
                double hProduction = ((NonRenewablePowerPlant) myAgent).gethProduction();
                double turnProduction = hProduction / 60 * TimeUtils.getTurnDuration();

                
				double givenEnergy = 0;
				if(requestedEnergy < turnProduction){
                    givenEnergy = requestedEnergy;
                } else {
					givenEnergy = turnProduction;
				}
                
				HashMap<String, Object> content = new HashMap<String, Object>();
        		content.put(MessageUtil.GIVEN_ENERGY, givenEnergy);
                ((NonRenewablePowerPlant) myAgent).createAndSendReply(ACLMessage.AGREE, receivedMessage, content);

				finished = true;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
        }else{
            //((NonRenewablePowerPlant) myAgent).log("(null msg) - Blocking...");
            block();
        }
	}

    @Override
    public boolean done() {
        return finished;
    }
}
