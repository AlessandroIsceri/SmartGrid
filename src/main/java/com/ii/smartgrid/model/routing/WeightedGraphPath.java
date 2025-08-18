package com.ii.smartgrid.model.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeightedGraphPath {
    private List<Double> edgeCosts;
    private List<String> graphPath;

    public WeightedGraphPath() {
        this.graphPath = new ArrayList<>();
        this.edgeCosts = new ArrayList<>();
    }

    public WeightedGraphPath(WeightedGraphPath weightedGraphPath) {
        this();
        this.graphPath.addAll(weightedGraphPath.graphPath);
        this.edgeCosts.addAll(weightedGraphPath.edgeCosts);
    }

    public void addCost(double cost) {
        edgeCosts.add(cost);
    }

    public void addSource(String source) {
        graphPath.add(0, source);
    }

    public void addVertex(String vertex) {
        graphPath.add(vertex);
    }

    public List<Double> getEdgeCosts() {
        return edgeCosts;
    }

    public List<String> getGraphPath() {
        return graphPath;
    }

    public String getSource() {
        return this.graphPath.get(0);
    }

    public String getTarget() {
        return this.graphPath.get(this.graphPath.size() - 1);
    }

    public double getTotalCost() {
        double sum = 0;
        for (Double cost : edgeCosts) {
            sum += cost;
        }
        return sum;
    }

    public void reverse() {
        Collections.reverse(edgeCosts);
        Collections.reverse(graphPath);
    }

    @Override
    public String toString() {
        return "WeightedGraphPath [edgeCosts=" + edgeCosts + ", graphPath=" + graphPath + "]";
    }

}