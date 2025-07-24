package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyFromLoadManagerBehaviour extends Behaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private boolean finished = false;

    public ReceiveEnergyFromLoadManagerBehaviour(GridAgent gridAgent) {
        super(gridAgent);
    }
    
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
        ACLMessage receivedMessage = myAgent.receive(mt);

        if(receivedMessage != null){
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMessage);  
			double givenEnergy = (double) jsonObject.get(MessageUtil.GIVEN_ENERGY);
            double neededEnergy = (double) jsonObject.get(MessageUtil.NEEDED_ENERGY);

            Grid grid = ((GridAgent) myAgent).getGrid();
            grid.addEnergy(givenEnergy);
            if (givenEnergy < neededEnergy) {
                ((CustomAgent) myAgent).log("Grid is losing energy", BEHAVIOUR_NAME);
            } else if (givenEnergy > neededEnergy){
                ((CustomAgent) myAgent).log("Grid is gaining energy", BEHAVIOUR_NAME);
            } else {
                ((CustomAgent) myAgent).log("Grid has stable energy", BEHAVIOUR_NAME);
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
