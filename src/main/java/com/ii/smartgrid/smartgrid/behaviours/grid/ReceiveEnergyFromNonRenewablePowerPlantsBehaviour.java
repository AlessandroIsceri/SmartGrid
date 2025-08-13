package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromNonRenewablePowerPlantsBehaviour extends CustomBehaviour{

    private int requestCont = 0;
    private boolean finished = false;
    private int nonRenewableActivePowerPlantCount; 

    public ReceiveEnergyFromNonRenewablePowerPlantsBehaviour(GridAgent gridAgent){
        super(gridAgent);
        Map<String, Boolean> nonRenewablePowerPlantActiveStatus = gridAgent.getGrid().getNonRenewablePowerPlantActiveStatus();
        nonRenewableActivePowerPlantCount = 0;
        for(String nonRenewablePowerPlantName : nonRenewablePowerPlantActiveStatus.keySet()){
            if(nonRenewablePowerPlantActiveStatus.get(nonRenewablePowerPlantName)){
                nonRenewableActivePowerPlantCount++;
            }
        }
    }

    @Override
    public void action() {
        if(nonRenewableActivePowerPlantCount == 0){
            ((CustomAgent) myAgent).log("NO RENEWABLE PP TO WAIT FOR", BEHAVIOUR_NAME);
            this.finished = true;
            return;
        }

		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);


        Grid grid = ((GridAgent) myAgent).getGrid();
        List<String> nonRenewablePowerPlantNames = grid.getNonRenewablePowerPlantNames();

        MessageTemplate mt;
        if(nonRenewablePowerPlantNames.isEmpty()){
            mt = mt1;
        }else{
            MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(nonRenewablePowerPlantNames.get(0), AID.ISLOCALNAME));
            for(int i = 1; i < nonRenewablePowerPlantNames.size(); i++){
                String smartHomeName = nonRenewablePowerPlantNames.get(i);
                mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(smartHomeName, AID.ISLOCALNAME)));
            }
            mt = MessageTemplate.and(mt1, mt2); 
        }

		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);
            requestCont++;
            
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
            ((CustomAgent) myAgent).log("receivedEnergy: " + receivedEnergy, BEHAVIOUR_NAME);
            
            // grid.removeExpectedConsumption(receivedEnergy);
            grid.addExpectedProduction(receivedEnergy);

            if(requestCont < nonRenewableActivePowerPlantCount){
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                finished = true;
            }
		} else {
			((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
		}
    }

    @Override
    public boolean done() {
        return finished;
    }
}

