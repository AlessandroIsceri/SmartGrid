package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlantInfo;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NonRenewablePowerPlantDiscoveryBehaviour extends CustomBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private int numberOfNonRenewablePowerPlants;
    private int requestCont;
    private boolean finished;

    public NonRenewablePowerPlantDiscoveryBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        this.numberOfNonRenewablePowerPlants = loadManagerAgent.getLoadManager().getNonRenewablePowerPlantNames().size();
        this.finished = false;
        this.requestCont = 0;
    }

    @Override
    public void action() {

        if(numberOfNonRenewablePowerPlants == 0){
            this.finished = true;
            return; 
        }

        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            if(!receivedMsg.getConversationId().contains("nonRenewable")){
                myAgent.putBack(receivedMsg);
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                ((CustomAgent) myAgent).log("Received a nonRenewablePP discovery msg from... " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);

                LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
                Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
                String nonRenewablePowerPlantName = receivedMsg.getSender().getLocalName();
                double maxTurnProduction = (double) jsonObject.get(MessageUtil.MAX_TURN_PRODUCTION);
                boolean on = (boolean) jsonObject.get(MessageUtil.ON); 
                NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo = new NonRenewablePowerPlantInfo(nonRenewablePowerPlantName, maxTurnProduction, on);
                loadManager.addNonRenewablePowerPlantInfo(nonRenewablePowerPlantInfo);

                if(requestCont < numberOfNonRenewablePowerPlants){
                    ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
                }else{
                    ((CustomAgent) myAgent).log("done", BEHAVIOUR_NAME);
                    loadManager.sortNonRenewablePowerPlantInfo();
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
