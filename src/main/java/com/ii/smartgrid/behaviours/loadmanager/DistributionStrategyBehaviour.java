package com.ii.smartgrid.behaviours.loadmanager;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.ii.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.model.entities.LoadManager;
import com.ii.smartgrid.model.entities.CustomObject.Priority;
import com.ii.smartgrid.model.routing.DistributionInstruction;
import com.ii.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.model.routing.WeightedGraphPath;

public abstract class DistributionStrategyBehaviour extends CustomOneShotBehaviour{

    protected WeightedGraphPath shortestPath;
    protected LoadManager loadManager;
    protected EnergyTransaction consumerNode;
    protected EnergyTransaction nearestProducerNode;
    protected List<? extends EnergyTransaction> producerNodes;
    protected List<? extends EnergyTransaction> consumerNodes;    
    protected LoadManagerAgent loadManagerAgent;

    protected DistributionStrategyBehaviour(LoadManagerAgent loadManagerAgent) {
        super(loadManagerAgent);
        shortestPath = null;
        this.loadManagerAgent = loadManagerAgent;
    }

    @Override
    public void action() { 
        loadManager = loadManagerAgent.getLoadManager();

        if(loadManager.areAllRequestsSatisfied()){
            // No requests to be satisfied
            log("There are no requests to be satisfied");
            return;
        }

        // Get all the producer nodes 
        producerNodes = this.getProducerNodes();

        if(producerNodes.isEmpty()){
            log("No producer nodes available");
            return;
        }

        // Iterate through the consumer grids by priority
        for(Priority priority : Priority.values()){
            // Get consumer nodes
            consumerNodes = loadManager.getConsumerNodesByPriority(priority);

            double priorityRequestedEnergySum = loadManager.getRequestedEnergySum(producerNodes, consumerNodes);

            // If the energy request for grid with current priority is too high, order consumers to satisfy as many requests as possible
            if(priorityRequestedEnergySum < 0){
                consumerNodes.sort(Comparator.comparingDouble(EnergyTransaction::getEnergyTransactionValue));
            }

            Iterator<? extends EnergyTransaction> consumerNodesIterator = consumerNodes.iterator();
            while(consumerNodesIterator.hasNext()){

                consumerNode = consumerNodesIterator.next();

                // Remove all consumer nodes that are producer nodes (i.e., those that have a battery and can send energy)
                if(producerNodes.contains(consumerNode)){
                    log("Consumer node " + consumerNode.getNodeName() + " is also a producer node, skipping it.");
                    continue;
                }

                double epsilon = 0.1;
                while(consumerNode.getEnergyTransactionValue() > epsilon){

                    // Find the shortest path to the nearest producer node
                    findPathToNearestProducer();

                    String nearestProducerNodeName = shortestPath.getSource();
                    nearestProducerNode = loadManager.getEnergyTransaction(nearestProducerNodeName);
                    
                    DistributionInstruction distributionInstruction = mainDistributionLogic();
        
                    // Save the path and the energy to distribute for nearest producer node
                    loadManager.addDistributionInstructions(nearestProducerNodeName, distributionInstruction);
                    
                    // If there are not any more energy producers, terminate the behaviour
                    if(producerNodes.isEmpty()){
                        return;
                    }
                }
            }
        }
        log("DistributionInfos : " + loadManager.getDistributionInstructions());
    }

    protected abstract DistributionInstruction mainDistributionLogic();

    protected abstract List<? extends EnergyTransaction> getProducerNodes();
    
    protected void findPathToNearestProducer(){
        // Find the nearest node that can send energy to the current consumer node
        double minCost = Double.MAX_VALUE;
        for(EnergyTransaction producerNode : producerNodes){
            WeightedGraphPath path = loadManager.getShortestPath(producerNode.getNodeName(), consumerNode.getNodeName());

            double curCost = path.getTotalCost();  
            if(curCost < minCost){
                minCost = curCost; 
                shortestPath = path;
            }
        }
    }
    
}
