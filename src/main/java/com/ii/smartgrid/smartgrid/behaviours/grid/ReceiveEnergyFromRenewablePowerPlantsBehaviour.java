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
    private GridAgent gridAgent;

    public ReceiveEnergyFromRenewablePowerPlantsBehaviour(GridAgent gridAgent){
        super(gridAgent);
        this.gridAgent = gridAgent;
        renewablePowerPlantCount = gridAgent.getGrid().getRenewablePowerPlantNames().size();
    }

    @Override
    public void action() {

        if(renewablePowerPlantCount == 0){
            this.finished = true;
            return;
        }

		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        
        Grid grid = gridAgent.getGrid();
        List<String> renewablePowerPlantNames = grid.getRenewablePowerPlantNames();

        MessageTemplate mt;
        if(renewablePowerPlantNames.isEmpty()){
            mt = mt1;
        }else{
            MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(renewablePowerPlantNames.get(0), AID.ISLOCALNAME));
            for(int i = 1; i < renewablePowerPlantNames.size(); i++){
                String smartBuildingName = renewablePowerPlantNames.get(i);
                mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(smartBuildingName, AID.ISLOCALNAME)));
            }
            mt = MessageTemplate.and(mt1, mt2); 
        }

		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName());
            requestCont++;
			/**
			 * {
			 * 		"energy": 200.0
			 * }
			 */
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
            log("receivedEnergy: " + receivedEnergy);
            
            
            // grid.removeExpectedConsumption(receivedEnergy);
            grid.addExpectedProduction(receivedEnergy);

            if(requestCont < renewablePowerPlantCount){
                customAgent.blockBehaviourIfQueueIsEmpty(this);
            }else{
                finished = true;
            }
		} else {
			customAgent.blockBehaviourIfQueueIsEmpty(this);
		}
    }

    @Override
    public boolean done() {
        return finished;
    }
}
