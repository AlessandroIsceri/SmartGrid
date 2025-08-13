package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.RenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.RenewablePowerPlant;
import com.ii.smartgrid.smartgrid.model.SolarPowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public abstract class SendProducedEnergyToGridBehaviour extends OneShotBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendProducedEnergyToGridBehaviour(RenewablePowerPlantAgent renewablePowerPlantAgent){
        super(renewablePowerPlantAgent);
    }

    @Override
    public void action() {     
        ((CustomAgent) myAgent).log("Started", BEHAVIOUR_NAME);   
        RenewablePowerPlant renewablePowerPlant = ((RenewablePowerPlantAgent) myAgent).getRenewablePowerPlant();
        double expectedProduction = this.getHourlyProduction(renewablePowerPlant) * TimeUtils.getTurnDurationHours();
        String gridName = renewablePowerPlant.getGridName();
        Map<String, Object> content = new HashMap<String, Object>();
        // content.put(MessageUtil.GIVEN_ENERGY, expectedProduction);
        Cable cable = renewablePowerPlant.getCable(gridName);
        content.put(MessageUtil.GIVEN_ENERGY, cable.computeTransmittedPower(expectedProduction));

        ((CustomAgent) myAgent).createAndSend(ACLMessage.INFORM, gridName, content);
        ((CustomAgent) myAgent).log("Finished", BEHAVIOUR_NAME);
    }

    protected abstract double getHourlyProduction(RenewablePowerPlant renewablePowerPlant);

}
