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
import com.ii.smartgrid.smartgrid.agents.LoadManager;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SendPowerPlantRequestsBehaviour extends Behaviour{

    // private int lastRequestIndex = 0;
    private boolean firstRequest;
    private boolean finished = false;
    private Iterator<String> iterator; 

    public SendPowerPlantRequestsBehaviour(LoadManager loadManager) {
        super(loadManager);
        firstRequest = true;
        List<String> nonRenewablePowerPlantsInfo = ((LoadManager) myAgent).getNonRenewablePowerPlantNames();
        iterator = nonRenewablePowerPlantsInfo.iterator();
    }

    @Override
    public void action() {
        double expectedConsumption = ((LoadManager) myAgent).getExpectedConsumption();
        if(firstRequest){
            sendRequestToPP(iterator.next(), expectedConsumption);
            firstRequest = false;
        }else{
            MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                                                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
        
            ACLMessage receivedMessage = myAgent.receive(mt);
            if(receivedMessage != null){
                //map: <PPname, hProduction> 
                if(receivedMessage.getPerformative() == ACLMessage.AGREE){
                    ObjectMapper objectMapper = new ObjectMapper();
                    TypeReference<HashMap<String, Double>> typeRef = new TypeReference<HashMap<String, Double>>() {};
                    HashMap<String, Double> jsonObject;
                    try {
                        jsonObject = objectMapper.readValue(receivedMessage.getContent(), typeRef);
                        double receivedEnergy = jsonObject.get(MessageUtil.GIVEN_ENERGY);
                        ((LoadManager) myAgent).removeExpectedConsumption(receivedEnergy);
                    } catch (JsonMappingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (JsonProcessingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if(receivedMessage.getPerformative() == ACLMessage.REFUSE){
                    ((LoadManager) myAgent).log(receivedMessage.getSender().getLocalName() + " refused to send energy");
                }
                
                // NonRenewablePowerPlants given in a crescent order of output production capacity
                expectedConsumption = ((LoadManager) myAgent).getExpectedConsumption();
                if(expectedConsumption > 0){
                    if(iterator.hasNext()){
                        sendRequestToPP(iterator.next(), expectedConsumption);
                    } else{
                        ((LoadManager) myAgent).log("No more non renewable pp available");
                        finished = true;
                    }
                } else {
                    ((LoadManager) myAgent).log("Received all needed energy");
                    finished = true;
                }
            } else {
                block();
            }
        }
    }

    private void sendRequestToPP(String ppName, double expectedConsumption){        
        HashMap<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.REQUESTED_ENERGY, expectedConsumption);
        ((LoadManager) myAgent).createAndSend(ACLMessage.REQUEST, ppName, content);
        block();
    }

    @Override
    public boolean done() {
        return finished;
    }
}