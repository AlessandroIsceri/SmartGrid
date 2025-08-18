package com.ii.smartgrid.model.entities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedPseudograph;

import com.ii.smartgrid.model.Cable;
import com.ii.smartgrid.model.NonRenewablePowerPlantInfo;
import com.ii.smartgrid.model.routing.DistributionInstruction;
import com.ii.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.model.routing.EnergyTransactionWithBattery;
import com.ii.smartgrid.model.routing.WeightedGraphPath;
import com.ii.smartgrid.model.routing.EnergyTransaction.TransactionType;


public class LoadManager extends CustomObject {

    private List<String> gridNames;
    private List<String> nonRenewablePowerPlantNames;
    private Map<String, EnergyTransaction> gridRequestedEnergy;
    private Graph<String, DefaultWeightedEdge> graph;
    private DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra;
    private Map<String, WeightedGraphPath> shortestPaths;
    private Map<String, List<DistributionInstruction>> distributionInstructions;
    private List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos;
    private Map<String, List<Cable>> gridsCables;

    public LoadManager() {
        super();
        gridRequestedEnergy = new HashMap<>();
        graph = new WeightedPseudograph<>(DefaultWeightedEdge.class);
        shortestPaths = new HashMap<>();
        distributionInstructions = new HashMap<>();
        nonRenewablePowerPlantInfos = new ArrayList<>();
        nonRenewablePowerPlantNames = new ArrayList<>();
        gridNames = new ArrayList<>();
        gridsCables = new HashMap<>();
    }

    // Add the nodes and edge (with its cost) to the graph
    public void addCommunicationCost(String from, String to, double cost) {
        graph.addVertex(from);
        graph.addVertex(to);
        if (graph.getEdge(to, from) == null) {
            DefaultWeightedEdge edge = graph.addEdge(from, to);
            graph.setEdgeWeight(edge, cost);
        }
    }

    public void addDistributionInstructions(String nodeName, DistributionInstruction shortesPath) {
        // Add a DistributionInstruction
        List<DistributionInstruction> curNodeDistributionInstructions = distributionInstructions.get(nodeName);
        if (curNodeDistributionInstructions == null) {
            curNodeDistributionInstructions = new ArrayList<>();
        }
        curNodeDistributionInstructions.add(shortesPath);
        distributionInstructions.put(nodeName, curNodeDistributionInstructions);
    }

    public void addGridCables(String gridName, List<Cable> cables) {
        gridsCables.put(gridName, cables);
    }

    public void addGridRequestedEnergy(String sender, EnergyTransaction energyTransaction) {
        gridRequestedEnergy.put(sender, energyTransaction);
    }

    public void addNonRenewablePowerPlantInfo(NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo) {
        nonRenewablePowerPlantInfos.add(nonRenewablePowerPlantInfo);
    }

    // Check if all energy requests are already satisfied
    public boolean areAllRequestsSatisfied() {
        for (EnergyTransaction energyTransaction : gridRequestedEnergy.values()) {
            if (energyTransaction.getTransactionType() == TransactionType.RECEIVE && energyTransaction.getEnergyTransactionValue() > 0) {
                return false;
            }
        }
        return true;
    }

    public void computeDijkstraForAllNodes() {
        // Run Dijkstra's algorithm
        dijkstra = new DijkstraShortestPath<>(graph);
        for (String sourceGridName : gridNames) {
            for (String targetGridName : gridNames) {
                if (!sourceGridName.equals(targetGridName)) {
                    String key = targetGridName + "-" + sourceGridName;
                    if (shortestPaths.containsKey(key)) {
                        // Inverse Shortest Path already exists
                        continue;
                    }
                    WeightedGraphPath shortestPath = initShortestPath(sourceGridName, targetGridName);
                    shortestPaths.put(sourceGridName + "-" + targetGridName, shortestPath);
                }
            }
        }
        System.out.println("shortestPaths: " + shortestPaths);
    }

    // Compute all the energy lost during the transmission through the shortest path
    public double computeEnergyLoss(double powerSentWH, List<String> shortestPath) {
        double energyLossWH = 0;
        for (int i = 0; i < shortestPath.size() - 1; i++) {
            String source = shortestPath.get(i);
            String next = shortestPath.get(i + 1);
            Cable cable = this.getCableFromNodes(source, next);

            double newPowerSentWH = cable.computeTransmittedPower(powerSentWH);
            energyLossWH += (powerSentWH - newPowerSentWH);
            powerSentWH = newPowerSentWH;
        }
        return energyLossWH;
    }

    // Compute the energy required to satisfy a request taking into account the losses generated in the path
    public double computeEnergyToSatisfyRequest(double requestedEnergyWH, List<String> shortestPath) {
        for (int i = shortestPath.size() - 1; i > 0; i--) {
            String last = shortestPath.get(i);
            String prev = shortestPath.get(i - 1);
            Cable cable = this.getCableFromNodes(last, prev);
            requestedEnergyWH = cable.getEnergyToSatisfyRequest(requestedEnergyWH);
        }
        return requestedEnergyWH;
    }
    
    // Computes and returns the required energy to charge all the batteries with a charge level < 25% to 75%
    public double getBatteryRequiredEnergy() {
        double batteryRequiredEnergy = 0;
        List<EnergyTransactionWithBattery> gridsWithBattery = getGridsWithBattery();
        for (EnergyTransactionWithBattery gridWithBattery : gridsWithBattery) {
            if(gridWithBattery.getStateOfCharge() < 0.25){
                double needed = gridWithBattery.getMissingEnergyForThreshold(0.75);
                if (needed > 0) {
                    batteryRequiredEnergy += needed;
                }
            }
        }
        return batteryRequiredEnergy;
    }

    // Given two nodes, returns the cable that connects them
    public Cable getCableFromNodes(String node1, String node2) {
        List<Cable> cables = gridsCables.get(node1);
        for (Cable cable : cables) {
            boolean eq1 = cable.getFrom().equals(node1) && cable.getTo().equals(node2);
            boolean eq2 = cable.getTo().equals(node1) && cable.getFrom().equals(node2);
            if (eq1 || eq2) {
                return cable;
            }
        }
        return null;
    }

    // Return all the consumer nodes with selectedPriority
    public List<EnergyTransaction> getConsumerNodesByPriority(Priority selectedPriority) {
        List<EnergyTransaction> nodesByPriority = new ArrayList<>();
        for (EnergyTransaction energyTransaction : gridRequestedEnergy.values()) {
            if (energyTransaction.getPriority() == selectedPriority && energyTransaction.getTransactionType() == TransactionType.RECEIVE) {
                nodesByPriority.add(energyTransaction);
            }
        }
        return nodesByPriority;
    }

    public DijkstraShortestPath<String, DefaultWeightedEdge> getDijkstra() {
        return dijkstra;
    }

    public void setDijkstra(DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra) {
        this.dijkstra = dijkstra;
    }

    public Map<String, List<DistributionInstruction>> getDistributionInstructions() {
        return distributionInstructions;
    }

    public void setDistributionInstructions(Map<String, List<DistributionInstruction>> distributionInstructions) {
        this.distributionInstructions = distributionInstructions;
    }

    public List<DistributionInstruction> getDistributionInstructionsForGrid(String gridName) {
        return distributionInstructions.get(gridName);
    }

    public double getEdgeWeight(String source, String target) {
        DefaultWeightedEdge edge = graph.getEdge(source, target);
        return graph.getEdgeWeight(edge);
    }

    public <T extends EnergyTransaction> T getEnergyTransaction(String gridName) {
        return (T) gridRequestedEnergy.get(gridName);
    }

    public Graph<String, DefaultWeightedEdge> getGraph() {
        return graph;
    }

    public void setGraph(Graph<String, DefaultWeightedEdge> graph) {
        this.graph = graph;
    }

    public List<String> getGridNames() {
        return gridNames;
    }

    public void setGridNames(List<String> gridNames) {
        this.gridNames = gridNames;
    }

    public Map<String, EnergyTransaction> getGridRequestedEnergy() {
        return gridRequestedEnergy;
    }

    public void setGridRequestedEnergy(Map<String, EnergyTransaction> gridRequestedEnergy) {
        this.gridRequestedEnergy = gridRequestedEnergy;
    }

    public Map<String, List<Cable>> getGridsCables() {
        return gridsCables;
    }

    public void setGridsCables(Map<String, List<Cable>> gridsCables) {
        this.gridsCables = gridsCables;
    }

    // Return all the grid with a battery
    public List<EnergyTransactionWithBattery> getGridsWithBattery() {
        List<EnergyTransactionWithBattery> gridsWithBattery = new ArrayList<>();
        for (EnergyTransaction request : gridRequestedEnergy.values()) {
            if (request.isBatteryAvailable()) {
                if(((EnergyTransactionWithBattery) request).getStateOfCharge() > 0.01){
                    gridsWithBattery.add((EnergyTransactionWithBattery) request);
                }   
            }
        }
        return gridsWithBattery;
    }

    public List<NonRenewablePowerPlantInfo> getNonRenewablePowerPlantInfos() {
        return nonRenewablePowerPlantInfos;
    }

    public void setNonRenewablePowerPlantInfos(List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos) {
        this.nonRenewablePowerPlantInfos = nonRenewablePowerPlantInfos;
    }

    public List<String> getNonRenewablePowerPlantNames() {
        return nonRenewablePowerPlantNames;
    }

    public void setNonRenewablePowerPlantNames(List<String> nonRenewablePowerPlantNames) {
        this.nonRenewablePowerPlantNames = nonRenewablePowerPlantNames;
    }

    // Return the number of messages that each grid must receive during the follow routing instruction behaviour
    public int getNumberOfMessagesForGrid(String gridName) {
        int count = 0;
        for (String key : distributionInstructions.keySet()) {
            if (!gridName.equals(key)) {
                List<DistributionInstruction> curDistributionInstructions = distributionInstructions.get(key);
                for (DistributionInstruction distributionInstruction : curDistributionInstructions) {
                    if (distributionInstruction.containsVertex(gridName)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public List<EnergyTransaction> getProducerNodes() {
        List<EnergyTransaction> producerNodes = new ArrayList<>();
        for (EnergyTransaction energyTransaction : gridRequestedEnergy.values()) {
            if (energyTransaction.getTransactionType() == TransactionType.SEND) {
                producerNodes.add(energyTransaction);
            }
        }
        return producerNodes;
    }

    // Sums all energy production and subtract the consumption of each transaction
    public double getRequestedEnergySum(List<? extends EnergyTransaction> producerNodes, List<? extends EnergyTransaction> consumerNodes) {
        double sum = 0;
        for (EnergyTransaction energyTransaction : producerNodes) {
            if (energyTransaction.getTransactionType() == TransactionType.SEND) {
                sum = sum + energyTransaction.getEnergyTransactionValue();
            }
        }
        for (EnergyTransaction energyTransaction : consumerNodes) {
            if (energyTransaction.getTransactionType() == TransactionType.RECEIVE) {
                sum = sum - energyTransaction.getEnergyTransactionValue();
            }
        }
        return sum;
    }

    // Returns the shortest path from source to target
    public WeightedGraphPath getShortestPath(String source, String target) {
        WeightedGraphPath path = shortestPaths.get(source + "-" + target);
        if (path == null) {
            path = new WeightedGraphPath(shortestPaths.get(target + "-" + source));
            path.reverse();
            return path;
        }
        return path;
    }

    public Map<String, WeightedGraphPath> getShortestPaths() {
        return shortestPaths;
    }

    public void setShortestPaths(Map<String, WeightedGraphPath> shortestPaths) {
        this.shortestPaths = shortestPaths;
    }

    // Returns the shortest path from source node to target node as a list of nodes, with its associated weight (cost)
    private WeightedGraphPath initShortestPath(String source, String target) {
        GraphPath<String, DefaultWeightedEdge> shortestPath = dijkstra.getPath(source, target);
        if (shortestPath != null) {
            WeightedGraphPath weightedGraphPath = new WeightedGraphPath();
            List<DefaultWeightedEdge> shortestNodesPath = shortestPath.getEdgeList();
            weightedGraphPath.addSource(source);
            for (DefaultWeightedEdge edge : shortestNodesPath) {
                String lastInsertedNode = weightedGraphPath.getTarget();
                String vertex = graph.getEdgeSource(edge);
                if (vertex.equals(lastInsertedNode)) {
                    vertex = graph.getEdgeTarget(edge);
                }
                weightedGraphPath.addVertex(vertex);
                weightedGraphPath.addCost(graph.getEdgeWeight(edge));
            }
            return weightedGraphPath;
        }
        return null;
    }

    public void removeAllDistributionInstructions() {
        distributionInstructions.clear();
    }

    public void sortNonRenewablePowerPlantInfo() {
        nonRenewablePowerPlantInfos.sort(Comparator.comparingDouble(NonRenewablePowerPlantInfo::getMaxTurnProduction));
    }

}
