package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.EnergyTransactionWithBattery;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.WeightedGraphPath;
import com.ii.smartgrid.smartgrid.utils.EnergyUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.core.behaviours.OneShotBehaviour;

public class DistributeExcessEnergyBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public DistributeExcessEnergyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }


    @Override
    public void action() {
        LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
        ((CustomAgent) myAgent).log("Started", BEHAVIOUR_NAME);

        List<EnergyTransaction> producerNodes = loadManager.getProducerNodes();
        if(!producerNodes.isEmpty()){
            List<EnergyTransactionWithBattery> gridsWithBattery = loadManager.getGridsWithBattery();
            //Send all remaining energy to the nearest grid with a battery
            Iterator<EnergyTransaction> producerNodesIterator = producerNodes.iterator();

            
            while(producerNodesIterator.hasNext()){

                EnergyTransaction producerNode = producerNodesIterator.next();

                ((CustomAgent) myAgent).log("*********************", BEHAVIOUR_NAME);
                ((CustomAgent) myAgent).log("producerNodes: " + producerNodes, BEHAVIOUR_NAME);
                ((CustomAgent) myAgent).log("currentProducer: " + producerNode, BEHAVIOUR_NAME);
                ((CustomAgent) myAgent).log("gridWithBatteries: " + gridsWithBattery, BEHAVIOUR_NAME);

                if(gridsWithBattery.contains(producerNode)){
                    // The producer node already has a battery -> continue
                    // Add producer node battery into his battery 
                    ((EnergyTransactionWithBattery) producerNode).receiveBatteryEnergy(producerNode.getEnergyTransactionValue());
                    ((EnergyTransactionWithBattery) producerNode).sendEnergy(producerNode.getEnergyTransactionValue());
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

                    ((CustomAgent) myAgent).log("gridWithBattery: " + gridWithBattery, BEHAVIOUR_NAME);
                    WeightedGraphPath path = loadManager.getShortestPath(producerNode.getNodeName(), gridWithBattery.getNodeName());
                    double curCost = path.getTotalCost();
                    if(curCost < minCost){
                        minCost = curCost;
                        shortesPath = path;
                    }
                }

                if(shortesPath == null){
                    List<String> path = new ArrayList<String>();
                    path.add(producerNode.getNodeName());
                    DistributionInstruction distributionInstruction = new DistributionInstruction(path, 0);
                    loadManager.addDistributionInstructions(producerNode.getNodeName(), distributionInstruction);
                    continue;
                }

                String nearestGridWithBatteryName = shortesPath.getTarget();
                ((CustomAgent) myAgent).log("nearestGridWithBatteryName found: " + nearestGridWithBatteryName, BEHAVIOUR_NAME);
                ((CustomAgent) myAgent).log("shortest path: " + shortesPath, BEHAVIOUR_NAME);
                EnergyTransactionWithBattery nearestGridWithBattery = (EnergyTransactionWithBattery) loadManager.getEnergyTransaction(nearestGridWithBatteryName);

                double availableEnergy = producerNode.getEnergyTransactionValue();
                DistributionInstruction distributionInstruction = null;
                double epsilon = 1.0;
                double maxSendableEnergy = nearestGridWithBattery.getMissingEnergyForThreshold(EnergyTransactionWithBattery.FULL_BATTERY);
                 
                maxSendableEnergy = loadManager.computeEnergyToSatisfyRequest(maxSendableEnergy, shortesPath.getGraphPath());

                ((CustomAgent) myAgent).log("maxSendableEnergy: " + maxSendableEnergy, BEHAVIOUR_NAME);
                if(maxSendableEnergy > availableEnergy){
                    // All energy can be sended
                    distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), availableEnergy);
                    producerNode.sendEnergy(availableEnergy);

                    double lostEnergy = loadManager.computeEnergyLoss(availableEnergy, shortesPath.getGraphPath());
                    ((CustomAgent) myAgent).log("lostEnergy: " + lostEnergy, BEHAVIOUR_NAME);
                    double receivedEnergy = availableEnergy - lostEnergy;
                    nearestGridWithBattery.receiveBatteryEnergy(receivedEnergy);
                    
                    // Remove producer 
                    producerNodesIterator.remove();
                }else{
                    // Send maxSendableEnergy 
                    distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), maxSendableEnergy);
                    producerNode.sendEnergy(maxSendableEnergy);

                    double lostEnergy = loadManager.computeEnergyLoss(maxSendableEnergy, shortesPath.getGraphPath());
                    ((CustomAgent) myAgent).log("lostEnergy: " + lostEnergy, BEHAVIOUR_NAME);
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
        ((CustomAgent) myAgent).log("DistributionInstructions: " + loadManager.getDistributionInstructions(), BEHAVIOUR_NAME);
        ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
    }

}
