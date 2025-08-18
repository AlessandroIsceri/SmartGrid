package com.ii.smartgrid.behaviours.loadmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.model.entities.LoadManager;
import com.ii.smartgrid.utils.MessageUtil;

import jade.lang.acl.ACLMessage;

public class SendInstructionsToGridsBehaviour extends CustomOneShotBehaviour{

    private LoadManagerAgent loadManagerAgent;

    public SendInstructionsToGridsBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        this.loadManagerAgent = loadManagerAgent;
    }

    @Override
    public void action() {
        LoadManager loadManager = loadManagerAgent.getLoadManager();
        List<String> gridNames = loadManager.getGridNames();
        for(String gridName : gridNames){
            // Iterate through the grids and send corresponding distribution instructions
            Map<String, Object> content = new HashMap<>();
            content.put(MessageUtil.DISTRIBUTION_INSTRUCTIONS, loadManager.getDistributionInstructionsForGrid(gridName));
            content.put(MessageUtil.ACTIVE_NON_RENEWABLE_POWERPLANTS, loadManager.getNonRenewablePowerPlantInfos());
            content.put(MessageUtil.NUMBER_OF_MSGS_TO_RECEIVE, loadManager.getNumberOfMessagesForGrid(gridName));
            customAgent.createAndSend(ACLMessage.INFORM, gridName, content);
        }
        loadManager.removeAllDistributionInstructions();
    }
}
