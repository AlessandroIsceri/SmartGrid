package com.ii.smartgrid.smartgrid.behaviours.loadmanager;

import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.smartgrid.model.entities.LoadManager;
import com.ii.smartgrid.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.smartgrid.model.routing.EnergyTransaction.TransactionType;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

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
            requestCont++;
            Map<String, Object> jsonObject = customAgent.convertAndReturnContent(receivedMsg);
            EnergyTransaction energyTransaction = customAgent.readValueFromJson(jsonObject.get(MessageUtil.ENERGY_TRANSACTION), EnergyTransaction.class);
            double requestedEnergy = energyTransaction.getEnergyTransactionValue();
            double energyWithMargin = requestedEnergy;
            if(energyTransaction.getTransactionType() == TransactionType.RECEIVE){
                energyWithMargin = requestedEnergy + requestedEnergy * 0.05; // Add 5% bonus energy to the requested one
            }
            
            String sender = receivedMsg.getSender().getLocalName();
            LoadManager loadManager = loadManagerAgent.getLoadManager(); 
            // Add the EnergyTransaction to the loadManager
            energyTransaction.setEnergyTransactionValue(energyWithMargin);
            loadManager.addGridRequestedEnergy(sender, energyTransaction);

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
