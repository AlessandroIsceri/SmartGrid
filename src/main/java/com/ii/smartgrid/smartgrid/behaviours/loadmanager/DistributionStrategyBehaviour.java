package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.CustomObject.Priority;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.WeightedGraphPath;

public abstract class DistributionStrategyBehaviour extends CustomOneShotBehaviour{

    protected WeightedGraphPath shortesPath;
    protected LoadManager loadManager;
    protected EnergyTransaction consumerNode;
    protected EnergyTransaction nearestProducerNode;
    protected List<? extends EnergyTransaction> producerNodes;
    protected List<? extends EnergyTransaction> consumerNodes;    
    private LoadManagerAgent loadManagerAgent;

    protected DistributionStrategyBehaviour(LoadManagerAgent loadManagerAgent) {
        super(loadManagerAgent);
        shortesPath = null;
        this.loadManagerAgent = loadManagerAgent;
    }

    @Override
    public void action() { 
        loadManager = loadManagerAgent.getLoadManager();

        if(loadManager.areAllRequestsSatisfied()){
            //No requests to be satisfied
            log("There are no requests to be satisfied");
            return;
        }

        // Get producers 
        producerNodes = this.getProducerNodes();

        if(producerNodes.isEmpty()){
            log("No producer nodes available");
            return;
        }

        log("producerNodes: " + producerNodes);
        for(Priority priority : Priority.values()){
            // Get consumers 
            consumerNodes = loadManager.getConsumerNodesByPriority(priority);
            log("consumerNodes: " + consumerNodes);
            double priorityRequestedEnergySum = loadManager.getRequestedEnergySum(producerNodes, consumerNodes);

            // If energy request of current priority is too high, order consumers
            if(priorityRequestedEnergySum < 0){
                consumerNodes.sort(Comparator.comparingDouble(EnergyTransaction::getEnergyTransactionValue));
            }

            Iterator<? extends EnergyTransaction> consumerNodesIterator = consumerNodes.iterator();

            while(consumerNodesIterator.hasNext()){
                consumerNode = consumerNodesIterator.next();
                while(consumerNode.getEnergyTransactionValue() > 0){
                    findPathToNearestProducer();

                    String nearestProducerNodeName = shortesPath.getSource();
                    log("nearestProducerNodeName found: " + nearestProducerNodeName);
                    log("shortest path: " + shortesPath);
                    nearestProducerNode = loadManager.getEnergyTransaction(nearestProducerNodeName);
                    
                    DistributionInstruction distributionInstruction = mainDistributionLogic();
                    
                    // Save the path
                    loadManager.addDistributionInstructions(nearestProducerNodeName, distributionInstruction);
                    
                    //If there are not energy producers, terminate the behaviour
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
                shortesPath = path;
            }
        }
    }
    
}
