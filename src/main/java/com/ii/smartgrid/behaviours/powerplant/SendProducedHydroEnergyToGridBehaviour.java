package com.ii.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.agents.HydroPowerPlantAgent;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;
import com.ii.smartgrid.utils.EnergyMonitorUtil;

public class SendProducedHydroEnergyToGridBehaviour extends SendProducedEnergyToGridBehaviour{

    public SendProducedHydroEnergyToGridBehaviour(HydroPowerPlantAgent hydroPowerPlantAgent){
        super(hydroPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant hydroPowerPlant){
        double curTurnProduction = hydroPowerPlant.getHourlyProduction();
        EnergyMonitorUtil.addHydroProduction(curTurnProduction, renewablePowerPlantAgent.getCurTurn());
        return curTurnProduction;
    }
    
}
