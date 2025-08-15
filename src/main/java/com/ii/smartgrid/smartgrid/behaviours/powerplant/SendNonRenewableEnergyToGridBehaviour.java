package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.HashMap;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.NonRenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.behaviours.CustomOneShotBehaviour;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.model.NonRenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

import jade.lang.acl.ACLMessage;

public class SendNonRenewableEnergyToGridBehaviour extends CustomOneShotBehaviour{

    private NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent;
    
    public SendNonRenewableEnergyToGridBehaviour(NonRenewablePowerPlantAgent nonRenewablePowerPlantAgent) {
        super(nonRenewablePowerPlantAgent);
        this.nonRenewablePowerPlantAgent = nonRenewablePowerPlantAgent;
    }

    @Override
    public void action() {
        NonRenewablePowerPlant nonRenewablePowerPlant = nonRenewablePowerPlantAgent.getNonRenewablePowerPlant();

        double givenEnergy = nonRenewablePowerPlant.getHourlyProduction() * TimeUtils.getTurnDurationHours();

        Map<String, Object> content = new HashMap<>();
        String gridName = nonRenewablePowerPlant.getGridName();

        Cable cable = nonRenewablePowerPlant.getCable(gridName);
        content.put(MessageUtil.GIVEN_ENERGY, cable.computeTransmittedPower(givenEnergy));
        customAgent.createAndSend(ACLMessage.INFORM, gridName, content);
    }

}
