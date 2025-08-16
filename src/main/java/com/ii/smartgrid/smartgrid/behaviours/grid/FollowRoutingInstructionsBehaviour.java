package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FollowRoutingInstructionsBehaviour extends CustomBehaviour {

    private boolean finished;
    private int messageCont;
    private GridAgent gridAgent;

    public FollowRoutingInstructionsBehaviour(GridAgent gridAgent) {
        super(gridAgent);
        this.finished = false;
        this.messageCont = 0;
        this.gridAgent = gridAgent;
    }

    @Override
    public void onStart(){
        super.onStart();
        Grid grid = gridAgent.getGrid();
        int numberOfMessagesToReceive = grid.getNumberOfMessagesToReceive();
        log("Number of messages to receive: " + numberOfMessagesToReceive);
    }

    @Override
    public void action() {
        Grid grid = gridAgent.getGrid();
        int numberOfMessagesToReceive = grid.getNumberOfMessagesToReceive();

        if (numberOfMessagesToReceive == 0) {
            this.finished = true;
            return;
        }

        // Create a message template to match all connected grids
        MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

        List<String> gridNames = grid.getGridNames();

        MessageTemplate mt;
        if (gridNames.isEmpty()) {
            mt = mt1;
        } else {
            MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(gridNames.get(0), AID.ISLOCALNAME));
            for (int i = 1; i < gridNames.size(); i++) {
                String gridName = gridNames.get(i);
                mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(gridName, AID.ISLOCALNAME)));
            }
            mt = MessageTemplate.and(mt1, mt2);
        }

        ACLMessage receivedMsg = customAgent.receive(mt);
        if (receivedMsg != null) {
            log("Received routing instruction from " + receivedMsg.getSender().getLocalName());
            messageCont++;

            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);

            // Read the distribution instruction
            DistributionInstruction distributionInstruction = customAgent.readValueFromJson(
                    jsonObject.get(MessageUtil.DISTRIBUTION_INSTRUCTIONS), DistributionInstruction.class);

            double energyToDistribute = distributionInstruction.getEnergyToDistribute();

            if (distributionInstruction.pathSize() == 1) {
                // The current Grid is the designated receiver node
                grid.addExpectedProduction(energyToDistribute);
            } else {
                // The Grid has to forward the energy and the distribution instruction to the next node
                distributionInstruction.removeFirstElement();
                String receiverName = distributionInstruction.getFirstReceiver();

                Cable cable = grid.getCable(receiverName);
                double energyToDistributeWithLoss = cable.computeTransmittedPower(energyToDistribute);

                distributionInstruction.setEnergyToDistribute(energyToDistributeWithLoss);

                Map<String, Object> content = new HashMap<>();
                content.put(MessageUtil.DISTRIBUTION_INSTRUCTIONS, distributionInstruction);
                customAgent.createAndSend(ACLMessage.INFORM, receiverName, content);
            }

            if (messageCont < numberOfMessagesToReceive) {
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
