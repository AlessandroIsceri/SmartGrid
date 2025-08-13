package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.HydroPowerPlantAgent;
import com.ii.smartgrid.smartgrid.agents.RenewablePowerPlantAgent;
import com.ii.smartgrid.smartgrid.agents.WindPowerPlantAgent;
import com.ii.smartgrid.smartgrid.model.RenewablePowerPlant;
import com.ii.smartgrid.smartgrid.model.SolarPowerPlant;
import com.ii.smartgrid.smartgrid.model.WindPowerPlant;
import com.ii.smartgrid.smartgrid.utils.MessageUtil;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendProducedHydroEnergyToGridBehaviour extends SendProducedEnergyToGridBehaviour{

    private final String BEHAVIOUR_NAME = this.getClass().getSimpleName();

    public SendProducedHydroEnergyToGridBehaviour(HydroPowerPlantAgent hydroPowerPlantAgent){
        super(hydroPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant hydroPowerPlant){
        return hydroPowerPlant.getHourlyProduction();
    }
    
}
