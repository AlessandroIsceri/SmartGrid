package com.ii.smartgrid.smartgrid.behaviours.powerplant;

import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.agents.SolarPowerPlantAgent;
import com.ii.smartgrid.smartgrid.model.RenewablePowerPlant;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;

public class SendProducedSolarEnergyToGridBehaviour extends SendProducedEnergyToGridBehaviour{

    public SendProducedSolarEnergyToGridBehaviour(SolarPowerPlantAgent solarPowerPlantAgent){
        super(solarPowerPlantAgent);
    }
    
    @Override
    protected double getHourlyProduction(RenewablePowerPlant solarPowerPlant){
        WeatherStatus curWeather = customAgent.getCurWeather();
        int curTurn = customAgent.getCurTurn();
        return solarPowerPlant.getHourlyProduction(curWeather, curTurn);
    }
    
}