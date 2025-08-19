package com.ii.smartgrid.behaviours.loadmanager;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.model.NonRenewablePowerPlantInfo;
import com.ii.smartgrid.model.entities.LoadManager;
import com.ii.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NonRenewablePowerPlantDiscoveryBehaviour extends CustomBehaviour{

    private int numberOfNonRenewablePowerPlants;
    private int requestCont;
    private boolean finished;
    private LoadManagerAgent loadManagerAgent;

    public NonRenewablePowerPlantDiscoveryBehaviour(LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        this.numberOfNonRenewablePowerPlants = loadManagerAgent.getLoadManager().getNonRenewablePowerPlantNames().size();
        this.finished = false;
        this.requestCont = 0;
        this.loadManagerAgent = loadManagerAgent;
    }

    @Override
    public void action() {

        if(numberOfNonRenewablePowerPlants == 0){
            this.finished = true;
            return; 
        }
        LoadManager loadManager = loadManagerAgent.getLoadManager();
        List<String> nonRenewablePowerPlantNames = loadManager.getNonRenewablePowerPlantNames();

        // Create a message template to match all non-renewable power plants
        MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate mt2 = MessageTemplate.MatchSender(new AID(nonRenewablePowerPlantNames.get(0), AID.ISLOCALNAME));
        for(int i = 1; i < nonRenewablePowerPlantNames.size(); i++){
            String nonRenewablePowerPlantName = nonRenewablePowerPlantNames.get(i);
            mt2 = MessageTemplate.or(mt2, MessageTemplate.MatchSender(new AID(nonRenewablePowerPlantName, AID.ISLOCALNAME)));
        }
        MessageTemplate mt = MessageTemplate.and(mt1, mt2); 

		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            String nonRenewablePowerPlantName = receivedMsg.getSender().getLocalName();
            double maxTurnProduction = (double) jsonObject.get(MessageUtil.MAX_TURN_PRODUCTION);
            boolean on = (boolean) jsonObject.get(MessageUtil.ON); 

            // Create and add non-renewable power plant info
            NonRenewablePowerPlantInfo nonRenewablePowerPlantInfo = new NonRenewablePowerPlantInfo(nonRenewablePowerPlantName, maxTurnProduction, on);
            loadManager.addNonRenewablePowerPlantInfo(nonRenewablePowerPlantInfo);

            if(requestCont < numberOfNonRenewablePowerPlants){
                customAgent.blockBehaviourIfQueueIsEmpty(this);
            }else{
                loadManager.sortNonRenewablePowerPlantInfo();
                finished = true;
            }
		} else {
			customAgent.blockBehaviourIfQueueIsEmpty(this);
		}
    }

    @Override
    public boolean done() {
        return finished;
    }

}
