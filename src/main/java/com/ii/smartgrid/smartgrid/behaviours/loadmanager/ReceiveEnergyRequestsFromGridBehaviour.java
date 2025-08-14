package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyRequestsFromGridBehaviour extends CustomBehaviour{

    private boolean finished = false;
    private int requestCont = 0;
    private int gridCount;
    private LoadManagerAgent loadManagerAgent;

    public ReceiveEnergyRequestsFromGridBehaviour (LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        this.gridCount = loadManagerAgent.getLoadManager().getGridNames().size();
        this.loadManagerAgent = loadManagerAgent;
    }

    @Override
    public void action() {
        if(gridCount == 0){
            finished = true;
            return;
        }

		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = customAgent.receive(mt);
		if (receivedMsg != null) {
            log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName());
            requestCont++;
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            // double requestedEnergy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
            EnergyTransaction energyTransaction = customAgent.readValueFromJson(jsonObject.get(MessageUtil.ENERGY_TRANSACTION), EnergyTransaction.class);
            double requestedEnergy = energyTransaction.getEnergyTransactionValue();
            double energyWithMargin = requestedEnergy;
            if(energyTransaction.getTransactionType() == TransactionType.RECEIVE){
                energyWithMargin = requestedEnergy + requestedEnergy * 0.05; //add 5% bonus energy
            }
            log("Requested: " + requestedEnergy + "\twithMargin: " + energyWithMargin);

            
            String sender = receivedMsg.getSender().getLocalName();
            LoadManager loadManager = loadManagerAgent.getLoadManager(); 
            energyTransaction.setEnergyTransactionValue(energyWithMargin);
            loadManager.addGridRequestedEnergy(sender, energyTransaction);
            // loadManager.addExpectedConsumption(requestedEnergy);

            if(requestCont < gridCount){
                customAgent.blockBehaviourIfQueueIsEmpty(this);
            }else{
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
