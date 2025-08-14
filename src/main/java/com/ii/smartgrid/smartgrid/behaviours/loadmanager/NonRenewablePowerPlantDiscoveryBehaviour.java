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

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NonRenewablePowerPlantDiscoveryBehaviour extends CustomBehaviour{

    private int numberOfNonRenewablePowerPlants;
    private int requestCont;
    private boolean finished;
    private LoadManagerAgent loadManagerAgent;

    public NonRenewablePowerPlantDiscoveryBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        this.numberOfNonRenewablePowerPlants = loadManagerAgent.getLoadManager().getNonRenewablePowerPlantNames().size();
        this.finished = false;
        this.requestCont = 0;
        this.loadManagerAgent = loadManagerAgent;
    }

    @Override
    public void action() {

        if(numberOfNonRenewablePowerPlants == 0){
            this.finished = true;
            return; 
        }
        LoadManager loadManager = loadManagerAgent.getLoadManager();
        List<String> nonRenewablePowerPlantNames = loadManager.getNonRenewablePowerPlantNames();

        MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(nonRenewablePowerPlantNames.get(0), AID.ISLOCALNAME));
        for(int i = 1; i < nonRenewablePowerPlantNames.size(); i++){
            String nonRenewablePowerPlantName = nonRenewablePowerPlantNames.get(i);
            mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(nonRenewablePowerPlantName, AID.ISLOCALNAME)));
        }
        MessageTemplate mt = MessageTemplate.and(mt1, mt2); 


		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            log("Received a nonRenewablePP discovery msg from... " + receivedMsg.getSender().getLocalName());
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            String nonRenewablePowerPlantName = receivedMsg.getSender().getLocalName();
            double maxTurnProduction = (double) jsonObject.get(MessageUtil.MAX_TURN_PRODUCTION);
            boolean on = (boolean) jsonObject.get(MessageUtil.ON); 
            NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo = new NonRenewablePowerPlantInfo(nonRenewablePowerPlantName, maxTurnProduction, on);
            loadManager.addNonRenewablePowerPlantInfo(nonRenewablePowerPlantInfo);

            if(requestCont < numberOfNonRenewablePowerPlants){
                customAgent.blockBehaviourIfQueueIsEmpty(this);
            }else{
                log("done");
                loadManager.sortNonRenewablePowerPlantInfo();
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
