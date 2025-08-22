package com.ii.smartgrid.behaviours.loadmanager;

import java.util.Map;

import com.ii.smartgrid.agents.LoadManagerAgent;
import com.ii.smartgrid.behaviours.CustomBehaviour;
import com.ii.smartgrid.model.entities.LoadManager;
import com.ii.smartgrid.model.routing.EnergyTransaction;
import com.ii.smartgrid.model.routing.EnergyTransaction.TransactionType;
import com.ii.smartgrid.model.routing.EnergyTransactionWithBattery;
import com.ii.smartgrid.utils.MessageUtil;

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
            
            if(energyTransaction.isBatteryAvailable() && energyTransaction.getTransactionType() == TransactionType.RECEIVE){
                EnergyTransactionWithBattery energyTransactionWithBattery = (EnergyTransactionWithBattery) energyTransaction;

                double energyReceived = energyTransactionWithBattery.sendBatteryEnergy(energyTransactionWithBattery.getEnergyTransactionValue());
                energyTransactionWithBattery.receiveEnergy(energyReceived);
            }

            double requestedEnergy = energyTransaction.getEnergyTransactionValue();
            double energyWithMargin = requestedEnergy;
            LoadManager loadManager = loadManagerAgent.getLoadManager(); 

            if(energyTransaction.getTransactionType() == TransactionType.RECEIVE){
                energyWithMargin = requestedEnergy + requestedEnergy * 0.05; // Add 5% bonus energy to the requested one
            }
            
            String sender = receivedMsg.getSender().getLocalName();
            // Add the EnergyTransaction to the loadManager
            energyTransaction.setEnergyTransactionValue(energyWithMargin);
            loadManager.addGridRequestedEnergy(sender, energyTransaction);

            loadManager.addNextTurnExpectedConsumption((double) jsonObject.get(MessageUtil.NEXT_TURN_EXPECTED_CONSUMPTION));
            loadManager.addCurTurnEnergyProduction((double) jsonObject.get(MessageUtil.CURRENT_TURN_ENERGY_PRODUCTION));
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
