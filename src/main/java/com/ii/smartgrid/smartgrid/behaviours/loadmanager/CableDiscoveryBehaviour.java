package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CableDiscoveryBehaviour extends Behaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private int numberOfNodes;
    private int requestCont;
    // private double[][] adjacencyMatrix;
    
    private boolean finished;

    public CableDiscoveryBehaviour(LoadManagerAgent loadManagerAgent) {
        super(loadManagerAgent);
        this.numberOfNodes = loadManagerAgent.getLoadManager().getGridNames().size();
        loadManagerAgent.log("waiting for " + numberOfNodes + " messages", BEHAVIOUR_NAME);
        // this.adjacencyMatrix = new double[numberOfNodes][numberOfNodes];
        this.requestCont = 0;
        this.finished = false;
    }

    @Override
    public void action() {

        if(numberOfNodes == 0){
            this.finished = true;
            return; 
        }

        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            if(!receivedMsg.getConversationId().contains("cable")){
                myAgent.putBack(receivedMsg);
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                String gridName = receivedMsg.getSender().getLocalName();
                ((CustomAgent) myAgent).log("Received a cable discovery msg from... " + gridName, BEHAVIOUR_NAME);
                Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
                ArrayList<Cable> cables = ((CustomAgent) myAgent).readValueFromJson(jsonObject.get(MessageUtil.CABLE_COSTS), new TypeReference<ArrayList<Cable>>() {});
                requestCont++;
                //{"cable_costs": [{"cost": 450, "to": "Grid-2", "from": "Grid-1"}]}
                LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
                for(Cable cable : cables){
                    double cost = cable.computeTransmissionCost();
                    String to = cable.getTo();
                    String from = cable.getFrom();
                
                    loadManager.addCommunicationCost(from, to, cost);
                }

                loadManager.addGridCables(gridName, cables);

                if(requestCont < numberOfNodes){
                    ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
                }else{
                    ((CustomAgent) myAgent).log("done", BEHAVIOUR_NAME);
                    loadManager.computeDijkstraForAllNodes();
                    finished = true;
                }
            }
		} else {
			((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
		}
    }

    @Override
    public boolean done() {
        return finished;
    }

}
