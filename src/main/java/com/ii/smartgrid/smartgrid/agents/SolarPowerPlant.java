package com.ii.smartgrid.smartgrid.agents;

import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;

public class SolarPowerPlant extends RenewablePowerPlant{

    private double hProductionSunny;
	private double hProductionRainy;
	private double hProductionCloudy;

    @Override
    public void setup(){
        status = PPStatus.ON;
        Object[] args = this.getArguments();
        
        loadManagerName = (String) args[args.length - 6];
        hProductionSunny = Double.parseDouble((String) args[args.length - 5]);
        hProductionRainy = Double.parseDouble((String) args[args.length - 4]);
        hProductionCloudy = Double.parseDouble((String) args[args.length - 3]);
        double maxCapacity = Double.parseDouble((String) args[args.length - 2]);
        double storedEnergy = Double.parseDouble((String) args[args.length - 1]);
        
        battery = new Battery(maxCapacity, storedEnergy);


        addBehaviour(new RenewablePowerPlantBehaviour(this));
        this.log("Setup completed");
    }

    @Override
    public double getHProduction() {
		//Night time from 20:00  to 6:00
        int hour = TimeUtils.getHourFromTurn(curTurn);
        if(hour < 6 || hour > 19) {
			return 0;
		}
        
		switch(curWeather) {
			case SUNNY:
				return hProductionSunny;
			case RAINY:
				return hProductionRainy;
			case CLOUDY:
				return hProductionCloudy;
			default:
                log("Error: Weather not found");
				return 0;
		}
	}   

}
