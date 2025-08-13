package com.ii.smartgrid.smartgrid.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WeightedGraphPath{
    private List<Double> edgeCosts;
    private List<String> graphPath;

    public WeightedGraphPath(){
        this.graphPath = new ArrayList<String>();
        this.edgeCosts = new ArrayList<Double>();
    }

    public WeightedGraphPath(WeightedGraphPath weightedGraphPath) {
        this();
        this.graphPath.addAll(weightedGraphPath.graphPath);
        this.edgeCosts.addAll(weightedGraphPath.edgeCosts);
    }

    public void addSource(String source){
        graphPath.add(0, source);
    }

    public void addVertex(String vertex){
        graphPath.add(vertex);
    }

    public double getTotalCost(){
        double sum = 0;
        for(Double cost : edgeCosts){
            sum += cost;
        }
        return sum;
    }

    public void addCost(double cost){
        edgeCosts.add(cost);
    }

    public String getSource(){
        return this.graphPath.get(0);
    }

    public String getTarget(){
        return this.graphPath.get(this.graphPath.size() - 1);
    }

    public List<String> getGraphPath() {
        return graphPath;
    }

    @Override
    public String toString() {
        return "WeightedGraphPath [edgeCosts=" + edgeCosts + ", graphPath=" + graphPath + "]";
    }

    public void reverse() {
        Collections.reverse(edgeCosts);
        Collections.reverse(graphPath);
    }

    public List<Double> getEdgeCosts() {
        return edgeCosts;
    }

}