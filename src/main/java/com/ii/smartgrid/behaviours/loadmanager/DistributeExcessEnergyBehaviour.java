package com.ii.smartgrid.behaviours.loadmanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ii.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.model.entities.LoadManager;
import com.ii.smartgrid.model.routing.DistributionInstruction;
import com.ii.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.model.routing.EnergyTransactionWithBattery;
import com.ii.smartgrid.model.routing.WeightedGraphPath;

public class DistributeExcessEnergyBehaviour extends CustomOneShotBehaviour{

    private LoadManagerAgent loadManagerAgent;

    public DistributeExcessEnergyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        this.loadManagerAgent = loadManagerAgent;
    }


    @Override
    public void action() {
        // Send all remaining (extra) energy to the nearest grid with a battery

        LoadManager loadManager = loadManagerAgent.getLoadManager();
        // Get all the remaining producer nodes
        List<EnergyTransaction> producerNodes = loadManager.getProducerNodes();
        if(!producerNodes.isEmpty()){
            
            List<EnergyTransactionWithBattery> gridsWithBattery = loadManager.getGridsWithBattery();
            Iterator<EnergyTransaction> producerNodesIterator = producerNodes.iterator();
            
            while(producerNodesIterator.hasNext()){
                // Iterate through the producer nodes 
                EnergyTransaction producerNode = producerNodesIterator.next();

                if(gridsWithBattery.contains(producerNode)){
                    // The producer node has a battery -> Add the node's extra energy into its battery and remove it from Producers 
                    ((EnergyTransactionWithBattery) producerNode).receiveBatteryEnergy(producerNode.getEnergyTransactionValue());
                    (producerNode).sendEnergy(producerNode.getEnergyTransactionValue());
                    producerNodesIterator.remove();
                    loadManager.addDistributionInstructions(producerNode.getNodeName(), new DistributionInstruction(producerNode.getNodeName()));
                    continue;
                }
                double minCost = Double.MAX_VALUE;
                WeightedGraphPath shortestPath = null;

                Iterator<EnergyTransactionWithBattery> gridsWithBatteryIterator = gridsWithBattery.iterator();

                while(gridsWithBatteryIterator.hasNext()){
                    // Search the nearest grid with battery, the battery must be able to receive energy this turn and
                    // must have less than 75% charge
                    EnergyTransactionWithBattery gridWithBattery = gridsWithBatteryIterator.next();

                    if(gridWithBattery.isCharged(0.75) || gridWithBattery.hasReachedLimit()){
                        continue;
                    }

                    WeightedGraphPath path = loadManager.getShortestPath(producerNode.getNodeName(), gridWithBattery.getNodeName());
                    double curCost = path.getTotalCost();
                    if(curCost < minCost){
                        minCost = curCost;
                        shortestPath = path;
                    }
                }

                if(shortestPath == null){
                    // No grids with battery found -> empty distribution instructions for current producer
                    List<String> path = new ArrayList<>();
                    path.add(producerNode.getNodeName());
                    DistributionInstruction distributionInstruction = new DistributionInstruction(path, 0);
                    loadManager.addDistributionInstructions(producerNode.getNodeName(), distributionInstruction);
                    continue;
                }

                String nearestGridWithBatteryName = shortestPath.getTarget();
                EnergyTransactionWithBattery nearestGridWithBattery = (EnergyTransactionWithBattery) loadManager.getEnergyTransaction(nearestGridWithBatteryName);

                // Compute available energy and maxSendableEnergy
                double availableEnergy = producerNode.getEnergyTransactionValue();
                DistributionInstruction distributionInstruction;
                double epsilon = 1.0;
                double maxSendableEnergy = nearestGridWithBattery.getMissingEnergyForThreshold(EnergyTransactionWithBattery.FULL_BATTERY);
                 
                maxSendableEnergy = loadManager.computeEnergyToSatisfyRequest(maxSendableEnergy, shortestPath.getGraphPath());

                if(maxSendableEnergy > availableEnergy){
                    // All energy can be sent
                    distributionInstruction = new DistributionInstruction(shortestPath.getGraphPath(), availableEnergy);
                    producerNode.sendEnergy(availableEnergy);

                    double lostEnergy = loadManager.computeEnergyLoss(availableEnergy, shortestPath.getGraphPath());
                    double receivedEnergy = availableEnergy - lostEnergy;
                    nearestGridWithBattery.receiveBatteryEnergy(receivedEnergy);
                    
                    // Remove producer 
                    producerNodesIterator.remove();
                }else{
                    // Send maxSendableEnergy 
                    distributionInstruction = new DistributionInstruction(shortestPath.getGraphPath(), maxSendableEnergy);
                    producerNode.sendEnergy(maxSendableEnergy);

                    double lostEnergy = loadManager.computeEnergyLoss(maxSendableEnergy, shortestPath.getGraphPath());
                    double receivedEnergy = maxSendableEnergy - lostEnergy;
                    nearestGridWithBattery.receiveBatteryEnergy(receivedEnergy);
                    
                    // Remove producer 
                    if(producerNode.getEnergyTransactionValue() < epsilon){
                        producerNodesIterator.remove();
                    }
                }

                // Save the path
                loadManager.addDistributionInstructions(producerNode.getNodeName(), distributionInstruction);
                
                // If there are no energy producers, terminate the behaviour
                if(producerNodes.isEmpty()){
                    return;
                }

            }
        }
        log("DistributionInstructions: " + loadManager.getDistributionInstructions());
    }

}
