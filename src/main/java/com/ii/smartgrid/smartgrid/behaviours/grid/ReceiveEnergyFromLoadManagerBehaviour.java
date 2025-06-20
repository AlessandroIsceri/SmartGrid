package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromLoadManagerBehaviour extends Behaviour{

    private boolean finished = false;

    public ReceiveEnergyFromLoadManagerBehaviour(Grid grid) {
        super(grid);
    }
    
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
        ACLMessage receivedMessage = myAgent.receive(mt);

        if(receivedMessage != null){
            Map<String, Object> jsonObject = ((Grid) myAgent).convertAndReturnContent(receivedMessage);  
			double givenEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
            double neededEnergy = (double) jsonObject.get(MessageUtil.NEEDED_ENERGY);
            ((Grid) myAgent).addEnergy(givenEnergy);
            if (givenEnergy < neededEnergy) {
                ((Grid) myAgent).log("Grid is losing energy");
            } else if (givenEnergy > neededEnergy){
                ((Grid) myAgent).log("Grid is gaining energy");
            } else {
                ((Grid) myAgent).log("Grid has stable energy");
            }
            finished = true;
        }else{
            block();
        }
        
    }

    @Override
    public boolean done() {
        return finished;
    }
    
    
}
