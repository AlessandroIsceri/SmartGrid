package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent.GridStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlantInfo;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveRoutingInstructionsFromLoadManagerBehaviour extends CustomBehaviour{

    private boolean finished;
    private GridAgent gridAgent;

    public ReceiveRoutingInstructionsFromLoadManagerBehaviour(GridAgent gridAgent){
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
            log("Received a message FROM " + receivedMsg.getSender().getLocalName()); 
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            
            List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos = customAgent.readValueFromJson(jsonObject.get(MessageUtil.ACTIVE_NON_RENEWABLE_POWERPLANTS), new TypeReference<List<NonRenewablePowerPlantInfo>>() {});

            int numberOfMessagesToReceive = (int) jsonObject.get(MessageUtil.NUMBER_OF_MSGS_TO_RECEIVE);

            if(gridAgent.getGridStatus() == GridStatus.SEND){
                List<DistributionInstruction> distributionInstructions = customAgent.readValueFromJson(jsonObject.get(MessageUtil.DISTRIBUTION_INSTRUCTIONS), new TypeReference<List<DistributionInstruction>>() {});
                grid.setDistributionInstructions(distributionInstructions);
                log("Setting distributionInstructions to " + distributionInstructions);
            }
            
            grid.updateNonRenewablePowerPlantActiveStatus(nonRenewablePowerPlantInfos);
            log("Number of msgs to receive: " + numberOfMessagesToReceive);
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
