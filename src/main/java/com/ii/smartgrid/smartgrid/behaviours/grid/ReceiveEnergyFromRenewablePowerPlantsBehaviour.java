package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.Map;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromRenewablePowerPlantsBehaviour extends CustomBehaviour{
    
    private int requestCont = 0;
    private boolean finished = false;
    private int renewablePowerPlantCount; 

    public ReceiveEnergyFromRenewablePowerPlantsBehaviour(GridAgent gridAgent){
        super(gridAgent);
        renewablePowerPlantCount = gridAgent.getGrid().getRenewablePowerPlantNames().size();
    }

    @Override
    public void action() {

        if(renewablePowerPlantCount == 0){
            this.finished = true;
            return;
        }

		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        
        Grid grid = ((GridAgent) myAgent).getGrid();
        List<String> renewablePowerPlantNames = grid.getRenewablePowerPlantNames();

        MessageTemplate mt;
        if(renewablePowerPlantNames.isEmpty()){
            mt = mt1;
        }else{
            MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(renewablePowerPlantNames.get(0), AID.ISLOCALNAME));
            for(int i = 1; i < renewablePowerPlantNames.size(); i++){
                String smartHomeName = renewablePowerPlantNames.get(i);
                mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(smartHomeName, AID.ISLOCALNAME)));
            }
            mt = MessageTemplate.and(mt1, mt2); 
        }

		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);
            requestCont++;
			/**
			 * {
			 * 		"energy": 200.0
			 * }
			 */
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
            ((CustomAgent) myAgent).log("receivedEnergy: " + receivedEnergy, BEHAVIOUR_NAME);
            
            
            // grid.removeExpectedConsumption(receivedEnergy);
            grid.addExpectedProduction(receivedEnergy);

            if(requestCont < renewablePowerPlantCount){
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
