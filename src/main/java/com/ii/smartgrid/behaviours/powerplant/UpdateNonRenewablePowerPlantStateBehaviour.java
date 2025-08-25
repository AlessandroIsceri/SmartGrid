package com.ii.smartgrid.behaviours.powerplant;

import java.util.Map;

import com.ii.smartgrid.agents.NonRenewablePowerPlantAgent;
import com.ii.smartgrid.agents.NonRenewablePowerPlantAgent.NonRenewablePowerPlantState;
import com.ii.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.model.entities.NonRenewablePowerPlant;
import com.ii.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class UpdateNonRenewablePowerPlantStateBehaviour extends CustomBehaviour{

    private boolean finished;
    private NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent;

    public UpdateNonRenewablePowerPlantStateBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent){
        super(nonRenewablePowerPlantAgent);
        this.finished = false;
        this.nonRenewablePowerPlantAgent = nonRenewablePowerPlantAgent;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage receivedMsg = customAgent.receive(mt);
        // Update internal state if this powerplant has to be turned ON or OFF following given instructions
        if (receivedMsg != null) {
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            boolean on = (boolean) jsonObject.get(MessageUtil.ON);
            NonRenewablePowerPlant nonRenewablePowerPlant = nonRenewablePowerPlantAgent.getNonRenewablePowerPlant();
            double requiredEnergy = (double) jsonObject.get(MessageUtil.REQUIRED_ENERGY);
            NonRenewablePowerPlantState nonRenewablePowerPlantState;
            if(on){
                nonRenewablePowerPlantState = NonRenewablePowerPlantState.ON;
            } else {
                nonRenewablePowerPlantState = NonRenewablePowerPlantState.OFF;
            }
            nonRenewablePowerPlantAgent.setNonRenewablePowerPlantState(nonRenewablePowerPlantState);
            nonRenewablePowerPlant.setTurnRequest(requiredEnergy);
            finished = true;
        } else {
            block();
        }
    }

    @Override
    public boolean done() {
        return finished;
    }


}
