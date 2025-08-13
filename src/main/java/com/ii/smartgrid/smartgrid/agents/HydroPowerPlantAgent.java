package com.ii.smartgrid.smartgrid.agents;

import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedHydroEnergyToGridBehaviour;
import com.ii.smartgrid.smartgrid.model.HydroPowerPlant;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

public class HydroPowerPlantAgent extends RenewablePowerPlantAgent{
    
    @Override
    public void setup(){
 
        String hydroPowerPlantName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.HYDRO_POWERPLANTS_PATH, hydroPowerPlantName, HydroPowerPlant.class);
        
        this.referencedObject.addConnectedAgentName(this.getRenewablePowerPlant().getGridName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    protected SendProducedEnergyToGridBehaviour createSendEnergyBehaviourBehaviour(){
        return new SendProducedHydroEnergyToGridBehaviour(this);
    }

}