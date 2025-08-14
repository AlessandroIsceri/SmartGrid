package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
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
        log("waiting for " + numberOfNodes + " messages");
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

        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            if(!receivedMsg.getConversationId().contains("cable")){
                customAgent.putBack(receivedMsg);
                customAgent.blockBehaviourIfQueueIsEmpty(this);
            }else{
                String gridName = receivedMsg.getSender().getLocalName();
                log("Received a cable discovery msg from... " + gridName);
                Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
                ArrayList<Cable> cables = customAgent.readValueFromJson(jsonObject.get(MessageUtil.CABLE_COSTS), new TypeReference<ArrayList<Cable>>() {});
                requestCont++;
                //{"cable_costs": [{"cost": 450, "to": "Grid-2", "from": "Grid-1"}]}
                LoadManager loadManager = loadManagerAgent.getLoadManager();
                for(Cable cable : cables){
                    double cost = cable.computeTransmissionCost();
                    String to = cable.getTo();
                    String from = cable.getFrom();
                
                    loadManager.addCommunicationCost(from, to, cost);
                }

                loadManager.addGridCables(gridName, cables);

                if(requestCont < numberOfNodes){
                    customAgent.blockBehaviourIfQueueIsEmpty(this);
                }else{
                    log("done");
                    loadManager.computeDijkstraForAllNodes();
                    finished = true;
                }
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
