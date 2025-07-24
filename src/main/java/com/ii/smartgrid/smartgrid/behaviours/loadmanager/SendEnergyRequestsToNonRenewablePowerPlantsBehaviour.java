package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendEnergyRequestsToNonRenewablePowerPlantsBehaviour extends Behaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    // private int lastRequestIndex = 0;
    private boolean firstRequest;
    private boolean finished = false;
    private Iterator<String> iterator; 

    public SendEnergyRequestsToNonRenewablePowerPlantsBehaviour(LoadManagerAgent loadManagerAgent) {
        super(loadManagerAgent);
        firstRequest = true;
        List<String> nonRenewablePowerPlantsInfo = ((LoadManagerAgent) myAgent).getLoadManager().getNonRenewablePowerPlantNames();
        iterator = nonRenewablePowerPlantsInfo.iterator();
    }

    @Override
    public void action() {
        LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
        double expectedConsumption = loadManager.getExpectedConsumption();
        if(firstRequest){ //TODO: non bisogna anche aggiungere che expectedConsumption sia > 0?
            sendRequestToPP(iterator.next(), expectedConsumption);
            firstRequest = false;
        }else{
            MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                                                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        
            ACLMessage receivedMessage = myAgent.receive(mt);
            if(receivedMessage != null){
                //map: <PPname, hourlyProduction> 
                if(receivedMessage.getPerformative() == ACLMessage.AGREE){
                    Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMessage);
                    double receivedEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
                    loadManager.removeExpectedConsumption(receivedEnergy); 
                } else if(receivedMessage.getPerformative() == ACLMessage.REFUSE){
                    ((CustomAgent) myAgent).log(receivedMessage.getSender().getLocalName() + " refused to send energy", BEHAVIOUR_NAME);
                }
                
                // NonRenewablePowerPlants given in a crescent order of output production capacity
                expectedConsumption = loadManager.getExpectedConsumption();
                if(expectedConsumption > 0){
                    if(iterator.hasNext()){
                        sendRequestToPP(iterator.next(), expectedConsumption);
                    } else{
                        ((CustomAgent) myAgent).log("No more non renewable pp available", BEHAVIOUR_NAME);
                        finished = true;
                    }
                } else {
                    ((CustomAgent) myAgent).log("Received all needed energy", BEHAVIOUR_NAME);
                    finished = true;
                }
            } else {
                block();
            }
        }
    }

    private void sendRequestToPP(String ppName, double expectedConsumption){        
        Map<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.REQUESTED_ENERGY, expectedConsumption);
        ((CustomAgent) myAgent).createAndSend(ACLMessage.REQUEST, ppName, content);
        block();
    }

    @Override
    public boolean done() {
        return finished;
    }
}