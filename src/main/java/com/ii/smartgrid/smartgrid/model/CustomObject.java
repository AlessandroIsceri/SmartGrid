package com.ii.smartgrid.smartgrid.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class CustomObject{
    
    public enum Priority {@JsonProperty("HIGH") HIGH, @JsonProperty("MEDIUM") MEDIUM, @JsonProperty("LOW") LOW};
    
    protected Map<String, Cable> connectedAgents;
    protected Coordinates coordinates;

    public CustomObject(){
        super();
        connectedAgents = new HashMap<String, Cable>();
    }

    public void addCable(String receiver, Cable cable){
        this.connectedAgents.put(receiver, cable);
    }

    public Map<String, Cable> getConnectedAgents() {
        return connectedAgents;
    }

    public void addConnectedAgentName(String agentName){
        addCable(agentName, null);
    }

    public void addConnectedAgentNames(List<String> agentNames){
        for(String agentName : agentNames){
            addConnectedAgentName(agentName);
        }
    }

    public void setConnectedAgents(Map<String, Cable> connectedAgents) {
        this.connectedAgents = connectedAgents;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }
    
    public Cable getCable(String agentName){
        return connectedAgents.get(agentName);
    }

}
