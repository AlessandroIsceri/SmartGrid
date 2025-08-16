package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedSolarEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.model.RenewablePowerPlant;
import com.ii.smartgrid.smartgrid.model.SolarPowerPlant;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

public class SolarPowerPlantAgent extends RenewablePowerPlantAgent{

    @Override
    public void setup(){
        String solarPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.SOLAR_POWERPLANTS_PATH, solarPowerPlantName, SolarPowerPlant.class);
        
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
