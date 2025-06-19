package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.List;

import com.ii.smartgrid.smartgrid.agents.RenewablePowerPlant;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendProducedEnergyBehaviour extends OneShotBehaviour{

    public SendProducedEnergyBehaviour(RenewablePowerPlant renewablePowerPlant){
        super(renewablePowerPlant);
    }

    @Override
    public void action() {        
        double expectedProduction = ((RenewablePowerPlant) myAgent).getHProduction();

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        String loadManagerName = ((RenewablePowerPlant) myAgent).getLoadManagerName();
        message.addReceiver(new AID(loadManagerName, AID.ISLOCALNAME));
        message.setContent("{\"energy\" :" + expectedProduction + "}");
        myAgent.send(message);
    }
}
