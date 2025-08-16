package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.List;

import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.model.routing.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.routing.EnergyTransactionWithBattery;

public class DistributeBatteryEnergyBehaviour extends DistributionStrategyBehaviour{

    public DistributeBatteryEnergyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }

    @Override
    protected DistributionInstruction mainDistributionLogic() {

        // The energy available from the grids was not enough to satisfy all the requests, so the batteries' energy is used to try to satisfy the remaining ones

        // Update energy request of current node
        double neededEnergy = consumerNode.getEnergyTransactionValue();
        neededEnergy = loadManager.computeEnergyToSatisfyRequest(neededEnergy, shortesPath.getGraphPath());

        double epsilon = 1.0;
        
        // Compute the sendableEnergy
        double sendableEnergy = ((EnergyTransactionWithBattery) nearestProducerNode).sendBatteryEnergy(neededEnergy);
        DistributionInstruction distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), sendableEnergy);

        // Compute the received energy after loss
        double lostEnergy = loadManager.computeEnergyLoss(sendableEnergy, shortesPath.getGraphPath());
        double receivedEnergy = sendableEnergy - lostEnergy;

        consumerNode.receiveEnergy(receivedEnergy);
        
        // Remove producer if it has not enough energy left 
        if(nearestProducerNode.getEnergyTransactionValue() < epsilon){
            producerNodes.remove(nearestProducerNode);
        }
        return distributionInstruction;
    }

    @Override
    protected List<? extends EnergyTransaction> getProducerNodes() {
        return loadManager.getGridsWithBattery();
    }
}
