package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.CustomObject.Priority;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithBattery;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.WeightedGraphPath;
import com.ii.smartgrid.smartgrid.utils.EnergyUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.core.behaviours.OneShotBehaviour;

public class DistributeBatteryEnergyBehaviour extends DistributionStrategyBehaviour{

    //l'energia delle pp non è bastata per soddisfare tutte le richieste, quindi vengono usate le batterie fino a esaurimento

    public DistributeBatteryEnergyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }

    @Override
    protected DistributionInstruction mainDistributionLogic() {

        // Update energy request of current node
        double neededEnergy = consumerNode.getEnergyTransactionValue();

        neededEnergy = loadManager.computeEnergyToSatisfyRequest(neededEnergy, shortesPath.getGraphPath());

        // double availableEnergy = nearestProducerNode.getEnergyBatteryAvailable();
        double epsilon = 1.0;
        
        double sendableEnergy = ((EnergyTransactionWithBattery) nearestProducerNode).sendBatteryEnergy(neededEnergy);
        DistributionInstruction distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), sendableEnergy);

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
