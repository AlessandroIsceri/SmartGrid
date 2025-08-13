package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.LoadManager;
import com.ii.smartgrid.smartgrid.model.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveEnergyRequestsFromGridBehaviour extends Behaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    private boolean finished = false;
    private int requestCont = 0;
    private int gridCount;

    public ReceiveEnergyRequestsFromGridBehaviour (LoadManagerAgent loadManagerAgent){
        super(loadManagerAgent);
        gridCount = ((LoadManagerAgent) myAgent).getLoadManager().getGridNames().size();
    }

    @Override
    public void action() {
        if(gridCount == 0){
            finished = true;
            return;
        }

		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
		ACLMessage receivedMsg = myAgent.receive(mt);
		if (receivedMsg != null) {
            ((CustomAgent) myAgent).log("RECEIVED A MESSAGE FROM " + receivedMsg.getSender().getLocalName(), BEHAVIOUR_NAME);
            requestCont++;
            Map<String, Object> jsonObject = ((CustomAgent) myAgent).convertAndReturnContent(receivedMsg);
            // double requestedEnergy = (double) jsonObject.get(MessageUtil.REQUESTED_ENERGY);
            EnergyTransaction energyTransaction = ((CustomAgent) myAgent).readValueFromJson(jsonObject.get(MessageUtil.ENERGY_TRANSACTION), EnergyTransaction.class);
            double requestedEnergy = energyTransaction.getEnergyTransactionValue();
            double energyWithMargin = requestedEnergy;
            if(energyTransaction.getTransactionType() == TransactionType.RECEIVE){
                energyWithMargin = requestedEnergy + requestedEnergy * 0.05; //add 5% bonus energy
            }
            ((CustomAgent) myAgent).log("Requested: " + requestedEnergy + "\twithMargin: " + energyWithMargin, BEHAVIOUR_NAME);

            
            String sender = receivedMsg.getSender().getLocalName();
            LoadManager loadManager = ((LoadManagerAgent) myAgent).getLoadManager(); 
            energyTransaction.setEnergyTransactionValue(energyWithMargin);
            loadManager.addGridRequestedEnergy(sender, energyTransaction);
            // loadManager.addExpectedConsumption(requestedEnergy);

            if(requestCont < gridCount){
                ((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
            }else{
                ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
                finished = true;
            }
		} else {
			((CustomAgent) myAgent).blockBehaviourIfQueueIsEmpty(this);
		}
    }

    @Override
    public boolean done() {
        return finished;
    }
}
