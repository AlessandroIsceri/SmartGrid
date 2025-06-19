package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.HashMap;
import java.util.List;

import com.ii.smartgrid.smartgrid.agents.RenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

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
        String loadManagerName = ((RenewablePowerPlant) myAgent).getLoadManagerName();
        HashMap<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.GIVEN_ENERGY, expectedProduction);
        ((RenewablePowerPlant) myAgent).createAndSend(ACLMessage.INFORM, loadManagerName, content);
    }
}
