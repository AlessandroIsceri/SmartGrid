package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendProducedEnergyToGridBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendProducedSolarEnergyToGridBehaviour;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;
import com.ii.smartgrid.model.entities.SolarPowerPlant;
import com.ii.smartgrid.utils.JsonUtil;

public class SolarPowerPlantAgent extends RenewablePowerPlantAgent{

    @Override
    public void setup(){
        String solarPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.SOLAR_POWER_PLANTS_PATH, solarPowerPlantName, SolarPowerPlant.class);
        
        RenewablePowerPlant solarPowerPlant = getRenewablePowerPlant();
        solarPowerPlant.addConnectedAgentName(solarPowerPlant.getGridName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    protected SendProducedEnergyToGridBehaviour createSendEnergyBehaviourBehaviour(){
        return new SendProducedSolarEnergyToGridBehaviour(this);
    }

}
