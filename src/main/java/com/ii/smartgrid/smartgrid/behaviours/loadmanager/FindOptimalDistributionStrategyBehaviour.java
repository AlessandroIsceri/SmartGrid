package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Iterator;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.model.CustomObject.Priority;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.WeightedGraphPath;
import com.ii.smartgrid.smartgrid.utils.EnergyUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.core.behaviours.OneShotBehaviour;

public class FindOptimalDistributionStrategyBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();


    public FindOptimalDistributionStrategyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }

    @Override
    public void action() { 
        // grid ha valore di prio minimo tra le buildings connesse ad esso
        // grid connessa alla batteria prio minima con valore molto basso (capacità libera della batteria)
        // grafo con: costo di ogni arco, valore di ogni nodo (grid), priorità di ogni grid
        // somma dei valori di ogni nodo -> se è >= 0, dovrebbe essere possibile soddisfare tutti -> non serve ordinare
        // se è < 0 -> si ordina per priorità
        // divido i nodi in fornitori e richiedenti (se hanno valore pos o neg)
        // Pos = {i | v_i > 0} → nodi fornitori
        // i negativi sono da ordinare per priorità decrescente (più alta prima)
        // ordino tutti i nodi in base alla priorità crescente (1 è massima)
        // cerco il percorso minimo da un nodo fornitore ad uno richiedente (dijkstra) in ordine di priorità e continuo cosi finche il nodo richiedente non ha più bisogno
        // appena la somma di richieste di una priorità è troppo alta per essere soddisfatta, soddisfo il maggior numero di nodi in ordine, finche i produttori non sono 0
        ((CustomAgent) myAgent).log("Started", BEHAVIOUR_NAME);
        LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
        
        // Get producers 
        List<EnergyTransaction> producerNodes = loadManager.getProducerNodes();
        ((CustomAgent) myAgent).log("producerNodes: " + producerNodes, BEHAVIOUR_NAME);
        for(Priority priority : Priority.values()){

            // Get consumers 
            List<EnergyTransaction> consumerNodes = loadManager.getConsumerNodesByPriority(priority);
            ((CustomAgent) myAgent).log("consumerNodes: " + consumerNodes, BEHAVIOUR_NAME);
            double priorityRequestedEnergySum =  loadManager.getRequestedEnergySum(producerNodes, consumerNodes);
            
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
                    for(EnergyTransaction producerNode : producerNodes){
                        WeightedGraphPath path = loadManager.getShortestPath(producerNode.getNodeName(), consumerNode.getNodeName());

                        double curCost = path.getTotalCost();  
                        if(curCost < minCost){
                            minCost = curCost; 
                            shortesPath = path;
                        }
                    }

                    String nearestProducerNodeName = shortesPath.getSource();
                    ((CustomAgent) myAgent).log("nearestProducerNodeName found: " + nearestProducerNodeName, BEHAVIOUR_NAME);
                    ((CustomAgent) myAgent).log("shortest path: " + shortesPath, BEHAVIOUR_NAME);
                    EnergyTransaction nearestProducerNode = loadManager.getEnergyTransaction(nearestProducerNodeName);

                    // Update energy request of current node
                    double neededEnergy = consumerNode.getEnergyTransactionValue();

                    neededEnergy = loadManager.computeEnergyToSatisfyRequest(neededEnergy, shortesPath.getGraphPath());

                    ((CustomAgent) myAgent).log("new needed energy: " + neededEnergy, BEHAVIOUR_NAME);


                    double availableEnergy = nearestProducerNode.getEnergyTransactionValue();
                    double epsilon = 1.0;
                    DistributionInstruction distributionInstruction = null;
                    
                    // Enough energy for satisfying current consumer request
                    if(availableEnergy > neededEnergy){
                        distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), neededEnergy);
                        nearestProducerNode.sendEnergy(neededEnergy);

                        double lostEnergy = loadManager.computeEnergyLoss(neededEnergy, shortesPath.getGraphPath());
                        ((CustomAgent) myAgent).log("lostEnergy: " + lostEnergy, BEHAVIOUR_NAME);
                        double receivedEnergy = neededEnergy - lostEnergy;
                        consumerNode.receiveEnergy(receivedEnergy);
                        
                        // Remove producer if it has not enough energy left 
                        if(nearestProducerNode.getEnergyTransactionValue() < epsilon){
                            producerNodes.remove(nearestProducerNode);
                        }
                    // Missing energy for satisfying current consumer request, sends what the prudcer 
                    } else {
                        distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), availableEnergy);
                        nearestProducerNode.sendEnergy(availableEnergy);

                        double lostEnergy = loadManager.computeEnergyLoss(availableEnergy, shortesPath.getGraphPath());
                        ((CustomAgent) myAgent).log("lostEnergy: " + lostEnergy, BEHAVIOUR_NAME);
                        double receivedEnergy = availableEnergy - lostEnergy;

                        ((CustomAgent) myAgent).log("receivedEnergy: " + receivedEnergy, BEHAVIOUR_NAME);

                        consumerNode.receiveEnergy(receivedEnergy);
                        producerNodes.remove(nearestProducerNode);
                    }
                    
                    // Save the path
                    loadManager.addDistributionInstructions(nearestProducerNodeName, distributionInstruction);
                    
                    //If there are not energy producers, terminate the behaviour
                    if(producerNodes.isEmpty()){
                        return;
                    }
                }
            }
        }
        ((CustomAgent) myAgent).log("DistributionInfos : " + loadManager.getDistributionInstructions(), BEHAVIOUR_NAME);
        ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
    }

}