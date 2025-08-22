package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendProducedEnergyToGridBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendProducedPhotovoltaicEnergyToGridBehaviour;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;
import com.ii.smartgrid.model.entities.PhotovoltaicPowerPlant;
import com.ii.smartgrid.utils.JsonUtil;

public class PhotovoltaicPowerPlantAgent extends RenewablePowerPlantAgent{

    @Override
    public void setup(){
        String photovoltaicPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.PHOTOVOLTAIC_POWER_PLANTS_PATH, photovoltaicPowerPlantName, PhotovoltaicPowerPlant.class);
        
        RenewablePowerPlant photovoltaicPowerPlant = getRenewablePowerPlant();
        photovoltaicPowerPlant.addConnectedAgentName(photovoltaicPowerPlant.getGridName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    protected SendProducedEnergyToGridBehaviour createSendEnergyBehaviourBehaviour(){
        return new SendProducedPhotovoltaicEnergyToGridBehaviour(this);
    }

}
