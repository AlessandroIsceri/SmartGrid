package com.ii.smartgrid.behaviours.loadmanager;

import java.util.List;

import com.ii.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.model.routing.DistributionInstruction;
import com.ii.smartgrid.model.routing.EnergyTransaction;

public class DistributeGridEnergyBehaviour extends DistributionStrategyBehaviour{

    public DistributeGridEnergyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }

    @Override
    protected DistributionInstruction mainDistributionLogic() {
        // Update energy request of current node
        double neededEnergy = consumerNode.getEnergyTransactionValue();
        neededEnergy = loadManager.computeEnergyToSatisfyRequest(neededEnergy, shortestPath.getGraphPath());

        DistributionInstruction distributionInstruction;
        double availableEnergy = nearestProducerNode.getEnergyTransactionValue();
        
        double epsilon = 1.0;
        if(availableEnergy > neededEnergy){
            // Enough energy for satisfying current consumer request
            distributionInstruction = new DistributionInstruction(shortestPath.getGraphPath(), neededEnergy);
            nearestProducerNode.sendEnergy(neededEnergy);

            // Compute energy loss
            double lostEnergy = loadManager.computeEnergyLoss(neededEnergy, shortestPath.getGraphPath());
            double receivedEnergy = neededEnergy - lostEnergy;
            consumerNode.receiveEnergy(receivedEnergy);
            
            // Remove producer if it has not enough energy left 
            if(nearestProducerNode.getEnergyTransactionValue() < epsilon){
                producerNodes.remove(nearestProducerNode);
            }
        } else {
            // Missing energy for satisfying current consumer request, sends what the producer can provide 
            distributionInstruction = new DistributionInstruction(shortestPath.getGraphPath(), availableEnergy);
            nearestProducerNode.sendEnergy(availableEnergy);

            // Compute energy loss
            double lostEnergy = loadManager.computeEnergyLoss(availableEnergy, shortestPath.getGraphPath());
            double receivedEnergy = availableEnergy - lostEnergy;

            consumerNode.receiveEnergy(receivedEnergy);
            // Remove the producer from producer nodes
            producerNodes.remove(nearestProducerNode);
        }
        return distributionInstruction;
    }

    @Override
    protected List<? extends EnergyTransaction> getProducerNodes() {
        return loadManager.getProducerNodes();
    }

}