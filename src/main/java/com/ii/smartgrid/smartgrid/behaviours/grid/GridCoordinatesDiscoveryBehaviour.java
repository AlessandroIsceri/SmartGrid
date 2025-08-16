package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.entities.Grid;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;

public class GridCoordinatesDiscoveryBehaviour extends CoordinatesDiscoveryBehaviour{
    
    private GridAgent gridAgent;

    public GridCoordinatesDiscoveryBehaviour(GridAgent gridAgent) {
        super(gridAgent);
        this.gridAgent = gridAgent;
    }

    @Override
    protected void sendInformationToLoadManager(){
        Map<String, Object> content = new HashMap<>();
        Grid grid = gridAgent.getGrid();
        List<Cable> cables = grid.getConnectedGridsCables();
        content.put(MessageUtil.CABLE_COSTS, cables);
        String loadManagerName = gridAgent.getGrid().getLoadManagerName();
        customAgent.createAndSend(ACLMessage.INFORM, loadManagerName, content, MessageUtil.CONVERSATION_ID_CABLE_DISCOVERY);
        log("Sending cable info to " + loadManagerName);
    }
}
