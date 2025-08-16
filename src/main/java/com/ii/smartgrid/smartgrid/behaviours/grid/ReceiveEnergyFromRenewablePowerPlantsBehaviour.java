package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.entities.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromRenewablePowerPlantsBehaviour extends CustomBehaviour {

    private int requestCont = 0;
    private boolean finished = false;
    private int renewablePowerPlantCount;
    private GridAgent gridAgent;

    public ReceiveEnergyFromRenewablePowerPlantsBehaviour(GridAgent gridAgent) {
        super(gridAgent);
        this.gridAgent = gridAgent;
        renewablePowerPlantCount = gridAgent.getGrid().getRenewablePowerPlantNames().size();
    }

    @Override
    public void action() {

        if (renewablePowerPlantCount == 0) {
            this.finished = true;
            return;
        }

        // Create a message template to match all renewable powerplants
        MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

        Grid grid = gridAgent.getGrid();
        List<String> renewablePowerPlantNames = grid.getRenewablePowerPlantNames();

        MessageTemplate mt;
        if (renewablePowerPlantNames.isEmpty()) {
            mt = mt1;
        } else {
            MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(renewablePowerPlantNames.get(0), AID.ISLOCALNAME));
            for (int i = 1; i < renewablePowerPlantNames.size(); i++) {
                String smartBuildingName = renewablePowerPlantNames.get(i);
                mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(smartBuildingName, AID.ISLOCALNAME)));
            }
            mt = MessageTemplate.and(mt1, mt2);
        }

        ACLMessage receivedMsg = customAgent.receive(mt);
        if (receivedMsg != null) {
            requestCont++;
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
            log("Received energy: " + receivedEnergy);

            grid.addExpectedProduction(receivedEnergy);

            if (requestCont < renewablePowerPlantCount) {
                customAgent.blockBehaviourIfQueueIsEmpty(this);
            } else {
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
