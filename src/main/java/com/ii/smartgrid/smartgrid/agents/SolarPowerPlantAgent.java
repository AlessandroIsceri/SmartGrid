package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.agents.RenewablePowerPlantAgent.RenewablePowerPlantBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.CoordinatesDiscoveryBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.behaviours.powerplant.SendProducedSolarEnergyToLoadManagerBehaviour;
import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.model.RenewablePowerPlant;
import com.ii.smartgrid.smartgrid.model.SolarPowerPlant;
import com.ii.smartgrid.smartgrid.model.WindPowerPlant;
import com.ii.smartgrid.smartgrid.utils.JsonUtil;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;

public class SolarPowerPlantAgent extends RenewablePowerPlantAgent{

    @Override
    public void setup(){
        String solarPowerPlantName = this.getLocalName();

        this.referencedObject = JsonUtil.readJsonFile(JsonUtil.SOLAR_POWERPLANTS_PATH, solarPowerPlantName, SolarPowerPlant.class);
        
        this.referencedObject.addConnectedAgentName(this.getRenewablePowerPlant().getLoadManagerName());
        
        this.addBehaviour(new CoordinatesDiscoveryBehaviour(this));

        this.addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    protected SendProducedEnergyToLoadManagerBehaviour createSendEnergyBehaviourBehaviour(){
        return new SendProducedSolarEnergyToLoadManagerBehaviour(this);
    }

}
