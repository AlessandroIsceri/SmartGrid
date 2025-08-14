package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.smartgrid.agents.HydroPowerPlantAgent;
import com.ii.smartgrid.smartgrid.model.RenewablePowerPlant;

public class SendProducedHydroEnergyToGridBehaviour extends SendProducedEnergyToGridBehaviour{

    public SendProducedHydroEnergyToGridBehaviour(HydroPowerPlantAgent hydroPowerPlantAgent){
        super(hydroPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant hydroPowerPlant){
        return hydroPowerPlant.getHourlyProduction();
    }
    
}
