package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent.GridStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;

public class InitRoutingInstructionsBehaviour extends CustomOneShotBehaviour{

    public InitRoutingInstructionsBehaviour(GridAgent gridAgent){
        super(gridAgent);
    }


    @Override
    public void action() {
        Grid grid = ((GridAgent) myAgent).getGrid();

        if(((GridAgent) myAgent).getGridStatus() == GridStatus.RECEIVE){
            return;
        }
        
        List<DistributionInstruction> distributionInstructions = grid.getDistributionInstructions();

        ((CustomAgent) myAgent).log("Distribution Instructions: " + distributionInstructions, BEHAVIOUR_NAME);

        for(DistributionInstruction distributionInstruction : distributionInstructions){
            distributionInstruction.removeFirstElement();

            if(distributionInstruction.pathSize() != 0){
                String receiverName = distributionInstruction.getFirstReceiver();
                double energyToDistribute = distributionInstruction.getEnergyToDistribute(); //500

                Cable cable = grid.getCable(receiverName);
                double energyToDistributeWithLoss = cable.computeTransmittedPower(energyToDistribute);
                distributionInstruction.setEnergyToDistribute(energyToDistributeWithLoss);
                
                Map<String, Object> content = new HashMap<String, Object>();
                content.put(MessageUtil.DISTRIBUTION_INSTRUCTIONS, distributionInstruction);
                // content.put(MessageUtil.GIVEN_ENERGY, energyToDistributeWithLoss); //499
                grid.addExpectedConsumption(energyToDistribute);
                ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, receiverName, content);
            }
        }
    }
}
