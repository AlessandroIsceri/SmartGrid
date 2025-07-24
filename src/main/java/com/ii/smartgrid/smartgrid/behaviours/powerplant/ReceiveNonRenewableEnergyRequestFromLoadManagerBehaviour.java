package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.NonRenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveNonRenewableEnergyRequestFromLoadManagerBehaviour extends Behaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public ReceiveNonRenewableEnergyRequestFromLoadManagerBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent){
        super(nonRenewablePowerPlantAgent);
    }

    private boolean finished = false;

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage receivedMessage = myAgent.receive(mt);
        if(receivedMessage != null){

            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMessage);
            double requestedEnergy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
            
            NonRenewablePowerPlant nonRenewablePowerPlant = ((NonRenewablePowerPlantAgent) myAgent).getNonRenewablePowerPlant();
            nonRenewablePowerPlant.setRequestedEnergy(requestedEnergy);
            finished = true;
        }else{
            block();
        }
    }

    @Override
    public boolean done() {
        return finished;
    }
}
