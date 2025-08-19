package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendProducedEnergyToGridBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendProducedWindEnergyToGridBehaviour;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;
import com.ii.smartgrid.model.entities.WindPowerPlant;
import com.ii.smartgrid.utils.JsonUtil;

public class WindPowerPlantAgent extends RenewablePowerPlantAgent{

    @Override
    public void setup(){
        String windPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.WIND_POWER_PLANTS_PATH, windPowerPlantName, WindPowerPlant.class);

        RenewablePowerPlant windPowerPlant = getRenewablePowerPlant();
        windPowerPlant.addConnectedAgentName(windPowerPlant.getGridName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    protected SendProducedEnergyToGridBehaviour createSendEnergyBehaviourBehaviour(){
        return new SendProducedWindEnergyToGridBehaviour(this);
    }

}