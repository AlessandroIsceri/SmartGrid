package com.ii.smartgrid.behaviours.powerplant;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.agents.RenewablePowerPlantAgent;
import com.ii.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.model.Cable;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;
import com.ii.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.utils.TimeUtils;

import jade.lang.acl.ACLMessage;

public abstract class SendProducedEnergyToGridBehaviour extends CustomOneShotBehaviour{

    private RenewablePowerPlantAgent renewablePowerPlantAgent;

    protected SendProducedEnergyToGridBehaviour(RenewablePowerPlantAgent renewablePowerPlantAgent){
        super(renewablePowerPlantAgent);
        this.renewablePowerPlantAgent = renewablePowerPlantAgent;
    }

    @Override
    public void action() {     
        RenewablePowerPlant renewablePowerPlant = renewablePowerPlantAgent.getRenewablePowerPlant();
        // The energy produced by Renewable Powerplants is always sent to grids
        double expectedProduction = this.getHourlyProduction(renewablePowerPlant) * TimeUtils.getTurnDurationHours();
        String gridName = renewablePowerPlant.getGridName();
        Map<String, Object> content = new HashMap<>();
        Cable cable = renewablePowerPlant.getCable(gridName);
        content.put(MessageUtil.GIVEN_ENERGY, cable.computeTransmittedPower(expectedProduction));

        customAgent.createAndSend(ACLMessage.INFORM, gridName, content);
    }

    protected abstract double getHourlyProduction(RenewablePowerPlant renewablePowerPlant);

}
