package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
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

public class DistributeBatteryEnergyBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    //l'energia delle pp non è bastata per soddisfare tutte le richieste, quindi vengono usate le batterie fino a esaurimento

    public DistributeBatteryEnergyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }

    @Override
    public void action() {
        LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
        double requestedEnergySum = loadManager.getAllRequestedEnergySum();
        ((CustomAgent) myAgent).log("Started", BEHAVIOUR_NAME);
        if(requestedEnergySum > 0){
            //No requests to be satisfied
            ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
            return;
        }

        
        List<EnergyTransactionWithBattery> smartGridsWithBattery = loadManager.getGridsWithBattery();
        for(Priority priority : Priority.values()){

            // Get consumers 
            List<EnergyTransaction> consumerNodes = loadManager.getConsumerNodesByPriority(priority);
            double priorityRequestedEnergySum = loadManager.getRequestedEnergySum(smartGridsWithBattery, consumerNodes);

            // If energy request of current priority is too high, order consumers
            if(priorityRequestedEnergySum < 0){
                Collections.sort(consumerNodes, Comparator.comparingDouble(EnergyTransaction::getEnergyTransactionValue));
            }

            Iterator<EnergyTransaction> consumerNodesIterator = consumerNodes.iterator();


            while(consumerNodesIterator.hasNext()){
                EnergyTransaction consumerNode = consumerNodesIterator.next();
                while(consumerNode.getEnergyTransactionValue() > 0){
                    // Find the nearest node that can send energy to the current consumer node
                    WeightedGraphPath shortesPath = null;
                    double minCost = Double.MAX_VALUE;
                    for(EnergyTransactionWithBattery producerNode : smartGridsWithBattery){
                        WeightedGraphPath path = loadManager.getShortestPath(producerNode.getNodeName(), consumerNode.getNodeName());
                        double curCost = path.getTotalCost();
                        if(curCost < minCost){
                            minCost = curCost;
                            shortesPath = path;
                        }
                    }
                    String nearestProducerNodeName = shortesPath.getSource();
                    EnergyTransactionWithBattery nearestProducerNode = (EnergyTransactionWithBattery) loadManager.getEnergyTransaction(nearestProducerNodeName);

                    // Update energy request of current node
                    double neededEnergy = consumerNode.getEnergyTransactionValue();

                    neededEnergy = loadManager.computeEnergyToSatisfyRequest(neededEnergy, shortesPath.getGraphPath());

                    // double availableEnergy = nearestProducerNode.getEnergyBatteryAvailable();
                    double epsilon = 1.0;
                    DistributionInstruction distributionInstruction = null;
                    

                    double sendableEnergy = nearestProducerNode.sendBatteryEnergy(neededEnergy);
                    distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), sendableEnergy);

                    double lostEnergy = loadManager.computeEnergyLoss(sendableEnergy, shortesPath.getGraphPath());
                    double receivedEnergy = sendableEnergy - lostEnergy;

                    consumerNode.receiveEnergy(receivedEnergy);
                    
                    // Remove producer if it has not enough energy left 
                    if(nearestProducerNode.getEnergyTransactionValue() < epsilon){
                        smartGridsWithBattery.remove(nearestProducerNode);
                    }

                    // Enough energy for satisfying current consumer request
                    // if(neededEnergy + availableEnergy > 0){
                    //     distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), neededEnergy);
                    //     nearestProducerNode.sendBatteryEnergy(neededEnergy);

                    //     double lostEnergy = computeEnergyLoss(neededEnergy, shortesPath.getGraphPath());
                    //     double receivedEnergy = neededEnergy - lostEnergy;

                    //     consumerNode.receiveEnergy(receivedEnergy);
                        
                    //     // Remove producer if it has not enough energy left 
                    //     if(nearestProducerNode.getEnergyTransactionValue() < epsilon){
                    //         smartGridsWithBattery.remove(nearestProducerNode);
                    //     }
                    // // Missing energy for satisfying current consumer request, sends what the prudcer 
                    // } else {
                    //     distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), availableEnergy);
                    //     nearestProducerNode.sendBatteryEnergy(availableEnergy);

                    //     double lostEnergy = computeEnergyLoss(availableEnergy, shortesPath.getGraphPath());
                    //     double receivedEnergy = availableEnergy - lostEnergy;

                    //     consumerNode.receiveEnergy(receivedEnergy);
                    //     smartGridsWithBattery.remove(nearestProducerNode);
                    // }
                    
                    // Save the path
                    loadManager.addDistributionInstructions(nearestProducerNodeName, distributionInstruction);
                    
                    //If there are not energy producers, terminate the behaviour
                    if(smartGridsWithBattery.isEmpty()){
                        ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
                        return;
                    }
                }
            }
        }
        ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
    }

}
