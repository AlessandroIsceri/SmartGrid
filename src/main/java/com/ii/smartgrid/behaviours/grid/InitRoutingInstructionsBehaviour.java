package com.ii.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.agents.GridAgent;
import com.ii.smartgrid.agents.GridAgent.GridStatus;
import com.ii.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.model.Cable;
import com.ii.smartgrid.model.entities.Grid;
import com.ii.smartgrid.model.routing.DistributionInstruction;
import com.ii.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;

public class InitRoutingInstructionsBehaviour extends CustomOneShotBehaviour {

    private GridAgent gridAgent;

    public InitRoutingInstructionsBehaviour(GridAgent gridAgent) {
        super(gridAgent);
        this.gridAgent = gridAgent;
    }

    @Override
    public void action() {
        Grid grid = gridAgent.getGrid();

        List<DistributionInstruction> distributionInstructions = grid.getDistributionInstructions();

        for (DistributionInstruction distributionInstruction : distributionInstructions) {
            distributionInstruction.removeFirstElement();

            if (distributionInstruction.pathSize() != 0) {
                // Forward the distribution instruction and the energy to the following node
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
