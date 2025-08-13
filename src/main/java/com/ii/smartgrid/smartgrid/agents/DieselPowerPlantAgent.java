package com.ii.smartgrid.smartgrid.agents;

import java.util.List;
import java.util.Map;

import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.NonRenewablePowerPlantCoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.model.DieselPowerPlant;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

public class DieselPowerPlantAgent extends NonRenewablePowerPlantAgent{
    
    // private DieselPowerPlant dieselPowerPlant;
    
    public void setup(){
        
        String dieselPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.DIESEL_POWERPLANTS_PATH, dieselPowerPlantName, DieselPowerPlant.class);
        
        ((DieselPowerPlant) this.referencedObject).setOn(false);

        DieselPowerPlant dieselPowerPlant = ((DieselPowerPlant) this.referencedObject);
        this.referencedObject.addConnectedAgentName(dieselPowerPlant.getGridName());
        
        this.addBehaviour(new NonRenewablePowerPlantCoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new NonRenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }
    
}
