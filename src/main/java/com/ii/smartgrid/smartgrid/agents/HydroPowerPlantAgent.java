package com.ii.smartgrid.smartgrid.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedHydroEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.smarthome.CheckSmartHomeMessagesBehaviour;
import com.ii.smartgrid.smartgrid.model.Appliance;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.HydroPowerPlant;
import com.ii.smartgrid.smartgrid.model.Routine;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;

public class HydroPowerPlantAgent extends RenewablePowerPlantAgent{
    
    @Override
    public void setup(){
 
        String hydroPowerPlantName = this.getLocalName();
        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.HYDRO_POWERPLANTS_PATH, hydroPowerPlantName, HydroPowerPlant.class);
        
        this.referencedObject.addConnectedAgentName(this.getRenewablePowerPlant().getLoadManagerName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));
        this.addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    protected SendProducedEnergyToLoadManagerBehaviour createSendEnergyBehaviourBehaviour(){
        return new SendProducedHydroEnergyToLoadManagerBehaviour(this);
    }

}