package com.ii.smartgrid.behaviours.grid;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.agents.GridAgent;
import com.ii.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.model.entities.Grid;
import com.ii.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromNonRenewablePowerPlantsBehaviour extends CustomBehaviour {

    private int requestCont = 0;
    private boolean finished = false;
    private int nonRenewableActivePowerPlantCount;
    private GridAgent gridAgent;

    public ReceiveEnergyFromNonRenewablePowerPlantsBehaviour(GridAgent gridAgent) {
        super(gridAgent);
        Map<String, Boolean> nonRenewablePowerPlantActiveStatus = gridAgent.getGrid().getNonRenewablePowerPlantActiveStatus();
        nonRenewableActivePowerPlantCount = 0;
        this.gridAgent = gridAgent;

        for (boolean isActive : nonRenewablePowerPlantActiveStatus.values()) {
            if (isActive) {
                nonRenewableActivePowerPlantCount++;
            }
        }

    }

    @Override
    public void action() {
        if (nonRenewableActivePowerPlantCount == 0) {
            log("No active non renewable powerplant");
            this.finished = true;
            return;
        }


        // Create a message template to match all non renewable powerplants
        MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

        Grid grid = gridAgent.getGrid();
        List<String> nonRenewablePowerPlantNames = grid.getNonRenewablePowerPlantNames();

        MessageTemplate mt;
        if (nonRenewablePowerPlantNames.isEmpty()) {
            mt = mt1;
        } else {
            MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(nonRenewablePowerPlantNames.get(0), AID.ISLOCALNAME));
            for (int i = 1; i < nonRenewablePowerPlantNames.size(); i++) {
                String smartBuildingName = nonRenewablePowerPlantNames.get(i);
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

            if (requestCont < nonRenewableActivePowerPlantCount) {
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
