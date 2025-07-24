package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WindSpeedStatus;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public abstract class CustomObject{
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
    
}
