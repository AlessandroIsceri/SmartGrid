package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.List;

import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.model.DistributionInstruction;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;

public class DistributeGridEnergyBehaviour extends DistributionStrategyBehaviour{

    public DistributeGridEnergyBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }

    @Override
    protected DistributionInstruction mainDistributionLogic() {
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

        // Update energy request of current node
        double neededEnergy = consumerNode.getEnergyTransactionValue();
        neededEnergy = loadManager.computeEnergyToSatisfyRequest(neededEnergy, shortesPath.getGraphPath());
        log("new needed energy: " + neededEnergy);

        double availableEnergy = nearestProducerNode.getEnergyTransactionValue();

        DistributionInstruction distributionInstruction = null;
        nearestProducerNode = loadManager.getEnergyTransaction(shortesPath.getSource());
        
        double epsilon = 1.0;
        // Enough energy for satisfying current consumer request
        if(availableEnergy > neededEnergy){
            distributionInstruction = new DistributionInstruction(shortesPath.getGraphPath(), neededEnergy);
            nearestProducerNode.sendEnergy(neededEnergy);

            double lostEnergy = loadManager.computeEnergyLoss(neededEnergy, shortesPath.getGraphPath());
            log("lostEnergy: " + lostEnergy);
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
            log("lostEnergy: " + lostEnergy);
            double receivedEnergy = availableEnergy - lostEnergy;

            log("receivedEnergy: " + receivedEnergy);

            consumerNode.receiveEnergy(receivedEnergy);
            producerNodes.remove(nearestProducerNode);
        }
        return distributionInstruction;
    }

    @Override
    protected List<? extends EnergyTransaction> getProducerNodes() {
        return loadManager.getProducerNodes();
    }

}