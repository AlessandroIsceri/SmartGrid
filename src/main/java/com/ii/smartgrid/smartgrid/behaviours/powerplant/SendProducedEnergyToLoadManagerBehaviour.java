package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.RenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.model.RenewablePowerPlant;
import com.ii.smartgrid.smartgrid.model.SolarPowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public abstract class SendProducedEnergyToLoadManagerBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendProducedEnergyToLoadManagerBehaviour(RenewablePowerPlantAgent renewablePowerPlantAgent){
        super(renewablePowerPlantAgent);
    }

    @Override
    public void action() {        
        RenewablePowerPlant renewablePowerPlant = ((RenewablePowerPlantAgent) myAgent).getRenewablePowerPlant();
        double expectedProduction = this.getHourlyProduction(renewablePowerPlant);
        String loadManagerName = renewablePowerPlant.getLoadManagerName();
        Map<String, Object> content = new HashMap<String, Object>();
        content.put(MessageUtil.GIVEN_ENERGY, expectedProduction);
        ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, loadManagerName, content);
    }

    protected abstract double getHourlyProduction(RenewablePowerPlant renewablePowerPlant);

}
