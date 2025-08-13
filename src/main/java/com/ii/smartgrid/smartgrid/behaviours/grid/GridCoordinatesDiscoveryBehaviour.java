package com.ii.smartgrid.smartgrid.behaviours.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.GridAgent;
import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;

public class GridCoordinatesDiscoveryBehaviour extends CoordinatesDiscoveryBehaviour{
    
    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public GridCoordinatesDiscoveryBehaviour(GridAgent gridAgent) {
        super(gridAgent);
    }

    @Override
    protected void sendInformationToLoadManager(){
        ((CustomAgent) myAgent).log("Started", BEHAVIOUR_NAME);
        Map<String, Object> content = new HashMap<String, Object>();
        List<Cable> cables = ((GridAgent) myAgent).getGrid().getConnectedGridsCables();
        content.put(MessageUtil.CABLE_COSTS, cables);
        String loadManagerName = ((GridAgent) myAgent).getGrid().getLoadManagerName();
        ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, loadManagerName, content, "cableDiscovery");
        ((CustomAgent) myAgent).log("Sending cable info to " + loadManagerName, BEHAVIOUR_NAME);
        ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
    }
}
