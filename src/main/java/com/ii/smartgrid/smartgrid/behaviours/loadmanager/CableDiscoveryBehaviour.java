package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CableDiscoveryBehaviour extends CustomBehaviour{

    private int numberOfNodes;
    private int requestCont;
    private LoadManagerAgent loadManagerAgent;
    
    private boolean finished;

    public CableDiscoveryBehaviour(LoadManagerAgent loadManagerAgent) {
        super(loadManagerAgent);
        this.numberOfNodes = loadManagerAgent.getLoadManager().getGridNames().size();
        this.requestCont = 0;
        this.finished = false;
        this.loadManagerAgent = loadManagerAgent;
    }

    @Override
    public void action() {

        if(numberOfNodes == 0){
            this.finished = true;
            return; 
        }

        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
                                                 MessageTemplate.MatchConversationId(MessageUtil.CONVERSATION_ID_CABLE_DISCOVERY));
		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            String gridName = receivedMsg.getSender().getLocalName();
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            ArrayList<Cable> cables = customAgent.readValueFromJson(jsonObject.get(MessageUtil.CABLE_COSTS), new TypeReference<ArrayList<Cable>>() {});
            requestCont++;
            LoadManager loadManager = loadManagerAgent.getLoadManager();
            for(Cable cable : cables){
                double cost = cable.computeTransmissionCost();
                String to = cable.getTo();
                String from = cable.getFrom();
            
                // Add the nodes and the edge to the graph
                loadManager.addCommunicationCost(from, to, cost);
            }

            loadManager.addGridCables(gridName, cables);

            if(requestCont < numberOfNodes){
                customAgent.blockBehaviourIfQueueIsEmpty(this);
            }else{
                // Compute all shortest paths using Dijkstra 
                loadManager.computeDijkstraForAllNodes();
                finished = true;
            }
		} else {
			customAgent.blockBehaviourIfQueueIsEmpty(this);
		}
    }

    @Override
    public boolean done() {
        return finished;
    }

}
