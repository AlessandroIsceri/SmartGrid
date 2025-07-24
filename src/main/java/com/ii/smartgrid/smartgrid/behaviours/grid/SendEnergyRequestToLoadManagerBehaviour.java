package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.model.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendEnergyRequestToLoadManagerBehaviour extends OneShotBehaviour{
    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendEnergyRequestToLoadManagerBehaviour(GridAgent gridAgent) {
        super(gridAgent);
    }

    @Override
    public void action() {
        Grid grid= ((GridAgent) myAgent).getGrid();
        String loadManagerName = grid.getLoadManagerName();
        Map<String, Object> content = new HashMap<String, Object>();
        if(grid.getExpectedConsumption() < 0){
            content.put(MessageUtil.REQUESTED_ENERGY, 0.0);
        }else{
            content.put(MessageUtil.REQUESTED_ENERGY, grid.getExpectedConsumption());
        }
        ((CustomAgent) myAgent).createAndSend(ACLMessage.REQUEST, loadManagerName, content);
    }

}
