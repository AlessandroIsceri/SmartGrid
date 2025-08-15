package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithBattery;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.WeightedGraphPath;

public class DistributeExcessEnergyBehaviour extends CustomOneShotBehaviour{

    private LoadManagerAgent loadManagerAgent;

    public DistributeExcessEnergyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        this.loadManagerAgent = loadManagerAgent;
    }


    @Override
    public void action() {
        LoadManager loadManager = loadManagerAgent.getLoadManager();

        List<EnergyTransaction> producerNodes = loadManager.getProducerNodes();
        if(!producerNodes.isEmpty()){
            List<EnergyTransactionWithBattery> gridsWithBattery = loadManager.getGridsWithBattery();
            //Send all remaining energy to the nearest grid with a battery
            Iterator<EnergyTransaction> producerNodesIterator = producerNodes.iterator();

            
            while(producerNodesIterator.hasNext()){

                EnergyTransaction producerNode = producerNodesIterator.next();

                log("*********************");
                log("producerNodes: " + producerNodes);
                log("currentProducer: " + producerNode);
                log("gridWithBatteries: " + gridsWithBattery);

                if(gridsWithBattery.contains(producerNode)){
                    // The producer node already has a battery -> continue
                    // Add producer node battery into his battery 
                    ((EnergyTransactionWithBattery) producerNode).receiveBatteryEnergy(producerNode.getEnergyTransactionValue());
                    (producerNode).sendEnergy(producerNode.getEnergyTransactionValue());
                    producerNodesIterator.remove();
                    loadManager.addDistributionInstructions(producerNode.getNodeName(), new DistributionInstruction(producerNode.getNodeName()));
                    continue;
                }
                double minCost = Double.MAX_VALUE;
                WeightedGraphPath shortesPath = null;

                Iterator<EnergyTransactionWithBattery> gridsWithBatteryIterator = gridsWithBattery.iterator();

                while(gridsWithBatteryIterator.hasNext()){
                    EnergyTransactionWithBattery gridWithBattery = gridsWithBatteryIterator.next();

                    if(gridWithBattery.isCharged(0.75) || gridWithBattery.hasReachedLimit()){
                        continue;
                    }

                    log("gridWithBattery: " + gridWithBattery);
                    WeightedGraphPath path = loadManager.getShortestPath(producerNode.getNodeName(), gridWithBattery.getNodeName());
                    double curCost = path.getTotalCost();
                    if(curCost < minCost){
                        minCost = curCost;
                        shortesPath = path;
                    }
                }

                if(shortesPath == null){
                    List<String> path = new ArrayList<>();
                    path.add(producerNode.getNodeName());
                    DistributionInstruction distributionInstruction = new DistributionInstruction(path, 0);
                    loadManager.addDistributionInstructions(producerNode.getNodeName(), distributionInstruction);
                    continue;
                }

                String nearestGridWithBatteryName = shortesPath.getTarget();
                log("nearestGridWithBatteryName found: " + nearestGridWithBatteryName);
                log("shortest path: " + shortesPath);
                EnergyTransactionWithBattery nearestGridWithBattery = (EnergyTransactionWithBattery) loadManager.getEnergyTransaction(nearestGridWithBatteryName);

                double availableEnergy = producerNode.getEnergyTransactionValue();
                DistributionInstruction distributionInstruction = null;
                double epsilon = 1.0;
                double maxSendableEnergy = nearestGridWithBattery.getMissingEnergyForThreshold(EnergyTransactionWithBattery.FULL_BATTERY);
                 
                maxSendableEnergy = loadManager.computeEnergyToSatisfyRequest(maxSendableEnergy, shortesPath.getGraphPath());

                log("maxSendableEnergy: " + maxSendableEnergy);
                if(maxSendableEnergy > availableEnergy){
                    // All energy can be sended
                    distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), availableEnergy);
                    producerNode.sendEnergy(availableEnergy);

                    double lostEnergy = loadManager.computeEnergyLoss(availableEnergy, shortesPath.getGraphPath());
                    log("lostEnergy: " + lostEnergy);
                    double receivedEnergy = availableEnergy - lostEnergy;
                    nearestGridWithBattery.receiveBatteryEnergy(receivedEnergy);
                    
                    // Remove producer 
                    producerNodesIterator.remove();
                }else{
                    // Send maxSendableEnergy 
                    distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), maxSendableEnergy);
                    producerNode.sendEnergy(maxSendableEnergy);

                    double lostEnergy = loadManager.computeEnergyLoss(maxSendableEnergy, shortesPath.getGraphPath());
                    log("lostEnergy: " + lostEnergy);
                    double receivedEnergy = maxSendableEnergy - lostEnergy;
                    nearestGridWithBattery.receiveBatteryEnergy(receivedEnergy);
                    
                    // Remove producer 
                    if(producerNode.getEnergyTransactionValue() < epsilon){
                        producerNodesIterator.remove();
                    }
                }

                // Save the path
                loadManager.addDistributionInstructions(producerNode.getNodeName(), distributionInstruction);
                
                //If there are not energy producers, terminate the behaviour
                if(producerNodes.isEmpty()){
                    return;
                }

            }
        }
        log("DistributionInstructions: " + loadManager.getDistributionInstructions());
    }

}
