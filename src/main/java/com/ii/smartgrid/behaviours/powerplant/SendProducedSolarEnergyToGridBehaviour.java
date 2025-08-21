package com.ii.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.agents.SolarPowerPlantAgent;
import com.ii.smartgrid.model.entities.RenewablePowerPlant;
import com.ii.smartgrid.utils.EnergyMonitorUtil;
import com.ii.smartgrid.utils.WeatherUtil.WeatherStatus;

public class SendProducedSolarEnergyToGridBehaviour extends SendProducedEnergyToGridBehaviour{

    public SendProducedSolarEnergyToGridBehaviour(SolarPowerPlantAgent solarPowerPlantAgent){
        super(solarPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant solarPowerPlant){
        WeatherStatus curWeather = customAgent.getCurWeather();
        int curTurn = customAgent.getCurTurn();
        double curTurnProduction = solarPowerPlant.getHourlyProduction(curWeather, curTurn);
        EnergyMonitorUtil.addSolarRenewableEnergyProduction(curTurnProduction, renewablePowerPlantAgent.getCurTurn());
        return curTurnProduction;
    }
    
}