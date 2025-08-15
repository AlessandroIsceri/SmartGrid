package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedPseudograph;

import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;


public class LoadManager extends CustomObject{

	private List<String> gridNames;
    private List<String> nonRenewablePowerPlantNames;
    private Map<String, EnergyTransaction> gridRequestedEnergy;
    private Graph <String, DefaultWeightedEdge> graph;
    private DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra;
    private Map<String, WeightedGraphPath> shortestPaths;
    private Map<String, List<DistributionInstruction>> distributionInstructions;
    private List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos;
    private Map<String, List<Cable>> gridsCables;

    public LoadManager(){
        super();
        gridRequestedEnergy = new HashMap<>();
        graph = new WeightedPseudograph <>(DefaultWeightedEdge.class);
        shortestPaths = new HashMap<>();
        distributionInstructions = new HashMap<>();
        nonRenewablePowerPlantInfos = new ArrayList<>();
        nonRenewablePowerPlantNames = new ArrayList<>();
        gridNames = new ArrayList<>();
        gridsCables = new HashMap<>();
    }

    public List<String> getNonRenewablePowerPlantNames() {
        return nonRenewablePowerPlantNames;
    }


    public void setNonRenewablePowerPlantNames(List<String> nonRenewablePowerPlantNames) {
        this.nonRenewablePowerPlantNames = nonRenewablePowerPlantNames;
    }


    public void setGridRequestedEnergy(Map<String, EnergyTransaction> gridRequestedEnergy) {
        this.gridRequestedEnergy = gridRequestedEnergy;
    }


    public Graph<String, DefaultWeightedEdge> getGraph() {
        return graph;
    }


    public void setGraph(Graph<String, DefaultWeightedEdge> graph) {
        this.graph = graph;
    }


    public DijkstraShortestPath<String, DefaultWeightedEdge> getDijkstra() {
        return dijkstra;
    }


    public void setDijkstra(DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra) {
        this.dijkstra = dijkstra;
    }


    public Map<String, WeightedGraphPath> getShortestPaths() {
        return shortestPaths;
    }


    public void setShortestPaths(Map<String, WeightedGraphPath> shortestPaths) {
        this.shortestPaths = shortestPaths;
    }


    public Map<String, List<DistributionInstruction>> getDistributionInstructions() {
        return distributionInstructions;
    }


    public void setDistributionInstructions(Map<String, List<DistributionInstruction>> distributionInstructions) {
        this.distributionInstructions = distributionInstructions;
    }


    public void setNonRenewablePowerPlantInfos(List<NonRenewablePowerPlantInfo> nonRenewablePowerPlantInfos) {
        this.nonRenewablePowerPlantInfos = nonRenewablePowerPlantInfos;
    }


    public List<String> getGridNames() {
        return gridNames;
    }

    public void setGridNames(List<String> gridNames) {
        this.gridNames = gridNames;
    }

    public void addGridRequestedEnergy(String sender, EnergyTransaction energyTransaction) {
        gridRequestedEnergy.put(sender, energyTransaction);
    }

    public Map<String, EnergyTransaction> getGridRequestedEnergy() {
        return gridRequestedEnergy;
    }

    public void addCommunicationCost(String from, String to, double cost){
        graph.addVertex(from);
        graph.addVertex(to);
        if(graph.getEdge(to, from) == null){
            DefaultWeightedEdge edge = graph.addEdge(from, to);
            graph.setEdgeWeight(edge, cost);
        }
    }

    public boolean areAllRequestsSatisfied(){
        for(EnergyTransaction energyTransaction : gridRequestedEnergy.values()){
            if(energyTransaction.getTransactionType() == TransactionType.RECEIVE && energyTransaction.getEnergyTransactionValue() > 0){
                return false;
            }
        }
        return true;
    }

    public double getRequestedEnergySum(List<? extends EnergyTransaction> producerNodes, List<? extends EnergyTransaction> consumerNodes) {
        double sum = 0;
        for(EnergyTransaction energyTransaction : producerNodes){
            if(energyTransaction.getTransactionType() == TransactionType.SEND){
                sum = sum + energyTransaction.getEnergyTransactionValue();
            }
        }
        for(EnergyTransaction energyTransaction : consumerNodes){
            if(energyTransaction.getTransactionType() == TransactionType.RECEIVE){
                sum = sum - energyTransaction.getEnergyTransactionValue();
            }
        }
        return sum;
    }

    public List<EnergyTransaction> getConsumerNodesByPriority(Priority selectedPriority) {
        List<EnergyTransaction> nodesByPriority = new ArrayList<>();
        for(EnergyTransaction energyTransaction : gridRequestedEnergy.values()){
            if(energyTransaction.getPriority() == selectedPriority && energyTransaction.getTransactionType() == TransactionType.RECEIVE){
                nodesByPriority.add(energyTransaction);
            }
        }
        return nodesByPriority; 
    }

    public List<EnergyTransaction> getProducerNodes() {
        List<EnergyTransaction> producerNodes = new ArrayList<>();
        for(EnergyTransaction energyTransaction : gridRequestedEnergy.values()){
            if(energyTransaction.getTransactionType() == TransactionType.SEND){
                producerNodes.add(energyTransaction);
            }
        }
        return producerNodes;
    }

    public void computeDijkstraForAllNodes() {

        // Run Dijkstra's algorithm
        dijkstra = new DijkstraShortestPath<>(graph);
        for(String sourceGridName : gridNames){
            for(String targetGridName : gridNames){
                if(!sourceGridName.equals(targetGridName)){
                    String key = targetGridName + "-" + sourceGridName;
                    if(shortestPaths.containsKey(key)){
                        continue;                      
                    }
                    WeightedGraphPath shortestPath = initShortestPath(sourceGridName, targetGridName);
                    shortestPaths.put(sourceGridName + "-" + targetGridName, shortestPath);
                }
            }
        }
        System.out.println("shortestPaths: " + shortestPaths);
    }

    public WeightedGraphPath getShortestPath(String source, String target){
        //Grid-3 - Grid-2 -> 
        WeightedGraphPath path = shortestPaths.get(source + "-" + target);
        if(path == null){
            path = new WeightedGraphPath(shortestPaths.get(target + "-" + source));
            path.reverse();
            return path;
        }
        return path;
    }

    private WeightedGraphPath initShortestPath(String source, String target){
        /*
         * ["grid-1", "grid-2", "grid-3"];
        */
        GraphPath<String, DefaultWeightedEdge> shortestPath = dijkstra.getPath(source, target);
        WeightedGraphPath weightedGraphPath = new WeightedGraphPath();
        List<DefaultWeightedEdge> shortestNodesPath = shortestPath.getEdgeList();
        if (shortestPath != null) {
            weightedGraphPath.addSource(source);
            for (DefaultWeightedEdge edge : shortestNodesPath) {
                String lastInsertedNode = weightedGraphPath.getTarget();
                String vertex = graph.getEdgeSource(edge);
                if(vertex.equals(lastInsertedNode)){
                    vertex = graph.getEdgeTarget(edge);
                }
                weightedGraphPath.addVertex(vertex);
                weightedGraphPath.addCost(graph.getEdgeWeight(edge));
            }
            return weightedGraphPath;
        }
        return null;
    }

    public <T extends EnergyTransaction> T getEnergyTransaction(String gridName){
         return (T) gridRequestedEnergy.get(gridName);
    }

    public void addDistributionInstructions(String nearestProducerNodeName, DistributionInstruction shortesPath) {
        // "A" -> [["A", "B", 100], ["A", "B", "C", 300]]
        List<DistributionInstruction> curNodeDistributionInstructions = distributionInstructions.get(nearestProducerNodeName);
        if(curNodeDistributionInstructions == null){
            curNodeDistributionInstructions = new ArrayList<>();
        }
        curNodeDistributionInstructions.add(shortesPath);
        distributionInstructions.put(nearestProducerNodeName, curNodeDistributionInstructions);
    }

    public List<EnergyTransactionWithBattery> getGridsWithBattery() {
        List<EnergyTransactionWithBattery> gridsWithBattery = new ArrayList<>();
        for(EnergyTransaction request : gridRequestedEnergy.values()){
            if(request.isBatteryAvailable()){
                gridsWithBattery.add((EnergyTransactionWithBattery) request);
            }
        }
        return gridsWithBattery;
    }

    public double getBatteryRequiredEnergy() {
        double batteryRequiredEnergy = 0;
        List<EnergyTransactionWithBattery> gridsWithBattery = getGridsWithBattery();
        for(EnergyTransactionWithBattery gridWithBattery : gridsWithBattery){
            double needed = gridWithBattery.getMissingEnergyForThreshold(0.75);
            if(needed > 0){
                batteryRequiredEnergy += needed;
            } 
        }
        return batteryRequiredEnergy;
    }


    public void sortNonRenewablePowerPlantInfo(){
        Collections.sort(nonRenewablePowerPlantInfos, Comparator.comparingDouble(NonRenewablePowerPlantInfo::getMaxTurnProduction));
    }

	public void addNonRenewablePowerPlantInfo(NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo) {
		nonRenewablePowerPlantInfos.add(nonRenewablePowerPlantInfo);
	}

    public List<NonRenewablePowerPlantInfo> getNonRenewablePowerPlantInfos(){
        return nonRenewablePowerPlantInfos;
    }


    public List<DistributionInstruction> getDistributionInstructionsForGrid(String gridName) {
        return distributionInstructions.get(gridName);
    }

    public int getNumberOfMessagesForGrid(String gridName) {
        int count = 0;
        for(String key : distributionInstructions.keySet()){
            if(!gridName.equals(key)){
                List<DistributionInstruction> curDistributionInstructions = distributionInstructions.get(key);
                for(DistributionInstruction distributionInstruction : curDistributionInstructions){
                    if(distributionInstruction.containsVertex(gridName)){
                        count ++;
                    }
                }
            }
        }
        return count;
    }

    public double getEdgeWeight(String source, String target){
        DefaultWeightedEdge edge =  graph.getEdge(source, target);
        return graph.getEdgeWeight(edge);
    }

    public void removeAllDistributionInstructions() {
        distributionInstructions.clear();
    }

    public void addGridCables(String gridName, List<Cable> cables) {
        gridsCables.put(gridName, cables);
    }

    public Map<String, List<Cable>> getGridsCables() {
        return gridsCables;
    }

    public void setGridsCables(Map<String, List<Cable>> gridsCables) {
        this.gridsCables = gridsCables;
    }

    public Cable getCableFromNodes(String node1, String node2) {
        List<Cable> cables = gridsCables.get(node1);
        for(Cable cable : cables){
            boolean eq1 = cable.getFrom().equals(node1) && cable.getTo().equals(node2);
            boolean eq2 = cable.getTo().equals(node1) && cable.getFrom().equals(node2);
            if(eq1 || eq2){
                return cable;
            }
        }
        return null;
    }


    public double computeEnergyToSatisfyRequest(double requestedEnergyWH, List<String> shortestPath){ 
        for(int i = shortestPath.size() - 1; i > 0; i--){
            String last = shortestPath.get(i);
            String prev = shortestPath.get(i - 1);
            Cable cable = this.getCableFromNodes(last, prev);
            requestedEnergyWH = cable.getEnergyToSatifyRequest(requestedEnergyWH);
        }
        return requestedEnergyWH;
    }
    
    
    public double computeEnergyLoss(double powerSentWH, List<String> shortestPath){
        double energyLossWH = 0; 
        for(int i = 0; i < shortestPath.size() - 1; i++){
            String source = shortestPath.get(i);
            String next = shortestPath.get(i + 1);
            Cable cable = this.getCableFromNodes(source, next);
            
            double newPowerSentWH = cable.computeTransmittedPower(powerSentWH);
            energyLossWH += (powerSentWH - newPowerSentWH);
            powerSentWH = newPowerSentWH;
        }
        return energyLossWH;
    }

}
