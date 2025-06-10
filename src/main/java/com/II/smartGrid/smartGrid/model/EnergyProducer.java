package com.II.smartGrid.smartGrid.model;

import com.II.smartGrid.smartGrid.tools.SimulationSettings.WeatherStatus;

public interface EnergyProducer {
	public abstract double getHProduction(WeatherStatus weather, int hour);
}
