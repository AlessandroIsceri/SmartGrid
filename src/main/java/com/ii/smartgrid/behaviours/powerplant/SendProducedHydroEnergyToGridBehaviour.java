package com.ii.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.agents.HydroPowerPlantAgent;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;

public class SendProducedHydroEnergyToGridBehaviour extends SendProducedEnergyToGridBehaviour{

    public SendProducedHydroEnergyToGridBehaviour(HydroPowerPlantAgent hydroPowerPlantAgent){
        super(hydroPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant hydroPowerPlant){
        return hydroPowerPlant.getHourlyProduction();
    }
    
}
