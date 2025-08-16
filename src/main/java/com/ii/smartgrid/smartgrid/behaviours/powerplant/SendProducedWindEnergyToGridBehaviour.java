package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.smartgrid.agents.WindPowerPlantAgent;
import com.ii.smartgrid.smartgrid.model.entities.RenewablePowerPlant;

public class SendProducedWindEnergyToGridBehaviour extends SendProducedEnergyToGridBehaviour{

    public SendProducedWindEnergyToGridBehaviour(WindPowerPlantAgent windPowerPlantAgent){
        super(windPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant windPowerPlant){
        return windPowerPlant.getHourlyProduction(customAgent.getCurWindSpeed());
    }
    
}
