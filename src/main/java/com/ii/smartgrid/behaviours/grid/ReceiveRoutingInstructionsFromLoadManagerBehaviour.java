package com.ii.smartgrid.behaviours.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ii.smartgrid.agents.GridAgent;
import com.ii.smartgrid.agents.GridAgent.GridStatus;
import com.ii.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.model.NonRenewablePowerPlantInfo;
import com.ii.smartgrid.model.entities.Grid;
import com.ii.smartgrid.model.routing.DistributionInstruction;
import com.ii.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveRoutingInstructionsFromLoadManagerBehaviour extends CustomBehaviour {

    private boolean finished;
    private GridAgent gridAgent;

    public ReceiveRoutingInstructionsFromLoadManagerBehaviour(GridAgent gridAgent) {
        super(gridAgent);
        this.finished = false;
        this.gridAgent = gridAgent;
    }

    @Override
    public void action() {
        Grid grid = gridAgent.getGrid();
        String loadManagerName = grid.getLoadManagerName();
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),       
                                                 MessageTemplate.MatchSender(new AID(loadManagerName, AID.ISLOCALNAME)));
        ACLMessage receivedMsg = customAgent.receive(mt);
        if (receivedMsg != null) {
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);

            // Receive information about which non-renewable power plant was turned on this turn
            List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos = customAgent.readValueFromJson(
                    jsonObject.get(MessageUtil.ACTIVE_NON_RENEWABLE_POWER_PLANTS),
                    new TypeReference<List<NonRenewablePowerPlantInfo>>() {}
                );

            int numberOfMessagesToReceive = (int) jsonObject.get(MessageUtil.NUMBER_OF_MESSAGES_TO_RECEIVE);
        
            List<DistributionInstruction> distributionInstructions = customAgent.readValueFromJson(
                    jsonObject.get(MessageUtil.DISTRIBUTION_INSTRUCTIONS),
                    new TypeReference<List<DistributionInstruction>>() {}
                );
            
            if(distributionInstructions == null){
                distributionInstructions = new ArrayList<>();
            }

            grid.setDistributionInstructions(distributionInstructions);

            // Update non-renewable power plant status
            grid.updateNonRenewablePowerPlantActiveStatus(nonRenewablePowerPlantInfos);
            grid.setNumberOfMessagesToReceive(numberOfMessagesToReceive);
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
