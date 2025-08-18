package com.ii.smartgrid.model.routing;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DistributionInstruction {

    private List<String> nodesPath;
    private double energyToDistribute;

    public DistributionInstruction(String node) {
        this();
        this.nodesPath.add(node);
    }

    public DistributionInstruction(List<String> nodesPath, double energyToDistribute) {
        this.nodesPath = nodesPath;
        this.energyToDistribute = energyToDistribute;
    }

    public DistributionInstruction() {
        super();
        this.nodesPath = new ArrayList<>();
        this.energyToDistribute = 0.0;
    }

    public boolean containsVertex(String gridName) {
        return nodesPath.contains(gridName);
    }

    public double getEnergyToDistribute() {
        return energyToDistribute;
    }

    public void setEnergyToDistribute(double energyToDistribute) {
        this.energyToDistribute = energyToDistribute;
    }

    @JsonIgnore
    public String getFirstReceiver() {
        return nodesPath.get(0);
    }

    public List<String> getNodesPath() {
        return nodesPath;
    }

    public void setNodesPath(List<String> nodesPath) {
        this.nodesPath = nodesPath;
    }

    public int pathSize() {
        return nodesPath.size();
    }

    public void removeFirstElement() {
        nodesPath.remove(0);
    }

    @Override
    public String toString() {
        return "DistributionInstruction [nodesPath=" + nodesPath + ", energyToDistribute=" + energyToDistribute + "]";
    }


}