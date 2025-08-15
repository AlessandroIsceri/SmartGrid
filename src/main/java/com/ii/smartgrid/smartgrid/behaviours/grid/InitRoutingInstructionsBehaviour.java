package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent.GridStatus;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;

public class InitRoutingInstructionsBehaviour extends CustomOneShotBehaviour{

    private GridAgent gridAgent;

    public InitRoutingInstructionsBehaviour(GridAgent gridAgent){
        super(gridAgent);
        this.gridAgent = gridAgent;
    }


    @Override
    public void action() {
        Grid grid = gridAgent.getGrid();

        if(gridAgent.getGridStatus() == GridStatus.RECEIVE){
            return;
        }
        
        List<DistributionInstruction> distributionInstructions = grid.getDistributionInstructions();

        log("Distribution Instructions: " + distributionInstructions);

        for(DistributionInstruction distributionInstruction : distributionInstructions){
            distributionInstruction.removeFirstElement();

            if(distributionInstruction.pathSize() != 0){
                String receiverName = distributionInstruction.getFirstReceiver();
                double energyToDistribute = distributionInstruction.getEnergyToDistribute();

                Cable cable = grid.getCable(receiverName);
                double energyToDistributeWithLoss = cable.computeTransmittedPower(energyToDistribute);
                distributionInstruction.setEnergyToDistribute(energyToDistributeWithLoss);
                
                Map<String, Object> content = new HashMap<>();
                content.put(MessageUtil.DISTRIBUTION_INSTRUCTIONS, distributionInstruction);
                grid.addExpectedConsumption(energyToDistribute);
                customAgent.createAndSend(ACLMessage.INFORM, receiverName, content);
            }
        }
    }
}
