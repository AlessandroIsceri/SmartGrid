package com.ii.smartgrid.behaviours.loadmanager;

import java.util.List;

import com.ii.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.model.routing.DistributionInstruction;
import com.ii.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.model.routing.EnergyTransactionWithBattery;

public class DistributeBatteryEnergyBehaviour extends DistributionStrategyBehaviour{

    public DistributeBatteryEnergyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }

    @Override
    protected DistributionInstruction mainDistributionLogic() {

        // The energy available from the grids was not enough to satisfy all the requests, so the batteries' energy is used to try to satisfy the remaining ones

        // Update energy request of current node
        double neededEnergy = consumerNode.getEnergyTransactionValue();
        neededEnergy = loadManager.computeEnergyToSatisfyRequest(neededEnergy, shortestPath.getGraphPath());

        // Compute the sendableEnergy
        double sendableEnergy = ((EnergyTransactionWithBattery) nearestProducerNode).sendBatteryEnergy(neededEnergy);
        DistributionInstruction distributionInstruction = new DistributionInstruction(shortestPath.getGraphPath(), sendableEnergy);

        // Compute the received energy after loss
        double lostEnergy = loadManager.computeEnergyLoss(sendableEnergy, shortestPath.getGraphPath());
        double receivedEnergy = sendableEnergy - lostEnergy;

        consumerNode.receiveEnergy(receivedEnergy);
        
        // Remove producer if it has not enough energy left 
        double epsilon = 0.01;
        if(((EnergyTransactionWithBattery) nearestProducerNode).getBattery().getStateOfCharge() < epsilon){
            producerNodes.remove(nearestProducerNode);
        }
        return distributionInstruction;
    }

    @Override
    protected List<? extends EnergyTransaction> getProducerNodes() {
        return loadManager.getGridsWithBatteryCharged();
    }
}
