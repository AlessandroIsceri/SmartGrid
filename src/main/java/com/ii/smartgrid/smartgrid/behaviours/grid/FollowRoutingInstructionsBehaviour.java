package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FollowRoutingInstructionsBehaviour extends Behaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();
    private boolean finished;
    private int messageCont;

    public FollowRoutingInstructionsBehaviour(GridAgent gridAgent){
        super(gridAgent);
        this.finished = false;
        this.messageCont = 0;
    }

    /*
     * Grid-1 : [1 - 3 - 5], 5
     * 
     * map
     */

    @Override
    public void action() {
        Grid grid = ((GridAgent) myAgent).getGrid();
        int numberOfMessagesToReceive = grid.getNumberOfMessagesToReceive();

        ((CustomAgent) myAgent).log("numberOfMessagesToReceive: " + numberOfMessagesToReceive, BEHAVIOUR_NAME);

        if(numberOfMessagesToReceive == 0){
            this.finished = true;
            return;
        }

		MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

        
        List<String> gridNames = grid.getGridNames();

        MessageTemplate mt;
        if(gridNames.isEmpty()){
            mt = mt1;
        }else{
            MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(gridNames.get(0), AID.ISLOCALNAME));
            for(int i = 1; i < gridNames.size(); i++){
                String gridName = gridNames.get(i);
                mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(gridName, AID.ISLOCALNAME)));
            }
            mt = MessageTemplate.and(mt1, mt2); 
        }

		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("RECEIVED A ROUTING INFO FROM " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);
            messageCont++;
        
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);

            // DistributionInstruction distributionInstruction = (DistributionInstruction) jsonObject.get(MessageUtil.DISTRIBUTION_INSTRUCTIONS);
            DistributionInstruction distributionInstruction = ((CustomAgent) myAgent).readValueFromJson(jsonObject.get(MessageUtil.DISTRIBUTION_INSTRUCTIONS), DistributionInstruction.class);

            double energyToDistribute = distributionInstruction.getEnergyToDistribute();

            if(distributionInstruction.pathSize() == 1){
                grid.addExpectedProduction(energyToDistribute);
            } else {
                distributionInstruction.removeFirstElement();
                String receiverName = distributionInstruction.getFirstReceiver();

                Cable cable = grid.getCable(receiverName);
                double energyToDistributeWithLoss = cable.computeTransmittedPower(energyToDistribute);

                distributionInstruction.setEnergyToDistribute(energyToDistributeWithLoss);

                Map<String, Object> content = new HashMap<String, Object>();
                content.put(MessageUtil.DISTRIBUTION_INSTRUCTIONS, distributionInstruction); 
                ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, receiverName, content);
            }

            if(messageCont < numberOfMessagesToReceive){
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
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
