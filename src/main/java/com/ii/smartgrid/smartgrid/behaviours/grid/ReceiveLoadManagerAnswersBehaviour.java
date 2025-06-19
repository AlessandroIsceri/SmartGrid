package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.Grid;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveLoadManagerAnswersBehaviour extends Behaviour{

    private boolean finished = false;

    public ReceiveLoadManagerAnswersBehaviour(Grid grid) {
        super(grid);
    }
    
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
        ACLMessage message = myAgent.receive(mt);

        if(message != null){
            String receivedContent = message.getContent();
        
            ObjectMapper objectMapper = new ObjectMapper();
			try {
				TypeReference<HashMap<String, Double>> typeRef = new TypeReference<HashMap<String, Double>>() {};
				HashMap<String, Double> jsonObject;
				jsonObject = objectMapper.readValue(receivedContent, typeRef);
				double givenEnergy = (double) jsonObject.get("givenEnergy");
                double neededEnergy = (double) jsonObject.get("neededEnergy");
                ((Grid) myAgent).addEnergy(givenEnergy);

                if (givenEnergy < neededEnergy) {
                    ((Grid) myAgent).log("Grid is losing energy");
                } else if (givenEnergy > neededEnergy){
                    ((Grid) myAgent).log("Grid is gaining energy");
                } else {
                    ((Grid) myAgent).log("Grid has stable energy");
                }
                finished = true;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
        }else{
            block();
        }
        
    }

    @Override
    public boolean done() {
        return finished;
    }
    
    
}
