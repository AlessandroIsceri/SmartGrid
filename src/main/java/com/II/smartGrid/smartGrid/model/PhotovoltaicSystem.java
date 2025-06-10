package com.II.smartGrid.smartGrid.model;

import com.II.smartGrid.smartGrid.tools.SimulationSettings.WeatherStatus;

public class PhotovoltaicSystem implements EnergyProducer{
	private double hProductionSunny;
	private double hProductionRainy;
	private double hProductionCloudy;
	
	public PhotovoltaicSystem(double hProductionSunny, double hProductionRainy, double hProductionCloudy) {
		super();
		this.hProductionSunny = hProductionSunny;
		this.hProductionRainy = hProductionRainy;
		this.hProductionCloudy = hProductionCloudy;
	}

	public double getHProduction(WeatherStatus weather, int hour) {
		//Night time from 20:00  to 6:00
		if(hour < 6 || hour > 19) {
			return 0;
		}
		switch(weather) {
			case SUNNY:
				return hProductionSunny;
			case RAINY:
				return hProductionRainy;
			case CLOUDY:
				return hProductionCloudy;
			default:
				return 0;
		}
	}

	@Override
	public String toString() {
		return "PhotovoltaicSystem [hProductionSunny=" + hProductionSunny + ", hProductionRainy=" + hProductionRainy
				+ ", hProductionCloudy=" + hProductionCloudy + "]";
	}
	
}
