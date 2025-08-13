package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendInstructionToGridsBehaviour extends CustomOneShotBehaviour{

    public SendInstructionToGridsBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
    }

    @Override
    public void action() {
        LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager();
        List<String> gridNames = loadManager.getGridNames();
        for(String gridName : gridNames){
            ((CustomAgent) myAgent).log("DistributionInstructionsForGrid " + gridName + " " + loadManager.getDistributionInstructionsForGrid(gridName), BEHAVIOUR_NAME);
            Map<String, Object> content = new HashMap<String, Object>();
            content.put(MessageUtil.DISTRIBUTION_INSTRUCTIONS, loadManager.getDistributionInstructionsForGrid(gridName));
            content.put(MessageUtil.ACTIVE_NON_RENEWABLE_POWERPLANTS, loadManager.getNonRenewablePowerPlantInfos());
            content.put(MessageUtil.NUMBER_OF_MSGS_TO_RECEIVE, loadManager.getNumberOfMessagesForGrid(gridName));
            ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, gridName, content);
        }
        // LoadManager loadManager = loadManagerAgent.getLoadManager();
        loadManager.removeAllDistributionInstructions();
    }
}
