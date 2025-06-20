package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveNonRenewableEnergyRequestFromLoadManagerBehaviour extends Behaviour{

    public ReceiveNonRenewableEnergyRequestFromLoadManagerBehaviour(NonRenewablePowerPlant nonRenewablePowerPlant){
        super(nonRenewablePowerPlant);
    }

    private boolean finished = false;

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage receivedMessage = myAgent.receive(mt);
        if(receivedMessage != null){
            //((NonRenewablePowerPlant) myAgent).log("Received a message");

            Map<String, Object> jsonObject = ((NonRenewablePowerPlant) myAgent).convertAndReturnContent(receivedMessage);
            double requestedEnergy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
            ((NonRenewablePowerPlant) myAgent).setRequestedEnergy(requestedEnergy);
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
