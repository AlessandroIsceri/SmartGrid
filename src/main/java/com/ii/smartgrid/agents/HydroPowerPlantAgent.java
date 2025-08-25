package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendProducedEnergyToGridBehaviour;
import com.ii.smartgrid.behaviours.powerplant.SendProducedHydroEnergyToGridBehaviour;
import com.ii.smartgrid.model.entities.HydroPowerPlant;
import com.ii.smartgrid.utils.JsonUtil;

public class HydroPowerPlantAgent extends RenewablePowerPlantAgent{

    @Override
    public void setup(){
 
        String hydroPowerPlantName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.HYDRO_POWER_PLANTS_PATH, hydroPowerPlantName, HydroPowerPlant.class);
        
        HydroPowerPlant hydroPowerPlant = (HydroPowerPlant) getRenewablePowerPlant();

        int randomSeed = (int) this.getArguments()[0];
        hydroPowerPlant.setRandomSeed(randomSeed);

        hydroPowerPlant.addConnectedAgentName(hydroPowerPlant.getGridName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    protected SendProducedEnergyToGridBehaviour createSendEnergyBehaviourBehaviour(){
        return new SendProducedHydroEnergyToGridBehaviour(this);
    }

}