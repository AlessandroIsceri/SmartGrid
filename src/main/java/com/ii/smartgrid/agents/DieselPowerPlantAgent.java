package com.ii.smartgrid.agents;

import com.ii.smartgrid.behaviours.powerplant.NonRenewablePowerPlantCoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.model.entities.DieselPowerPlant;
import com.ii.smartgrid.model.entities.NonRenewablePowerPlant;
import com.ii.smartgrid.utils.JsonUtil;

public class DieselPowerPlantAgent extends NonRenewablePowerPlantAgent{
        
    @Override
    public void setup(){
        String dieselPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.DIESEL_POWERPLANTS_PATH, dieselPowerPlantName, DieselPowerPlant.class);
        
        NonRenewablePowerPlant dieselPowerPlant = getNonRenewablePowerPlant();
        dieselPowerPlant.setOn(false);

        dieselPowerPlant.addConnectedAgentName(dieselPowerPlant.getGridName());

        this.addBehaviour(new NonRenewablePowerPlantCoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new NonRenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }
    
}
