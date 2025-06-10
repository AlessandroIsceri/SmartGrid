package com.II.smartGrid.smartGrid.tools;

import com.II.smartGrid.smartGrid.tools.SimulationSettings.WeatherStatus;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

public class UpdateWeather extends OneShotBehaviour {
	
	public UpdateWeather(SimulationSettings simulationSettings) {
		super(simulationSettings);
	}

	@Override
	public void action() {
		double randomNum = Math.random(); 
		WeatherStatus curWeatherStatus = ((SimulationSettings) myAgent).getCurWeatherStatus();
		
		// Current state	Sunny (S)		Cloudy (C)		Rainy (R)
		// Sunny (S)		0.7				0.2				0.1
		// Cloudy (C)		0.3				0.5				0.2
		// Rainy (R)		0.1				0.4				0.5
		
		
		if(curWeatherStatus == WeatherStatus.SUNNY) {
			if(randomNum < 0.7) {
				((SimulationSettings) myAgent).setCurWeatherStatus(WeatherStatus.SUNNY);
			}else if(randomNum < 0.9){
				((SimulationSettings) myAgent).setCurWeatherStatus(WeatherStatus.CLOUDY);
			}else {
				((SimulationSettings) myAgent).setCurWeatherStatus(WeatherStatus.RAINY);
			}
		}else if(curWeatherStatus == WeatherStatus.CLOUDY) {
			if(randomNum < 0.5) {
				((SimulationSettings) myAgent).setCurWeatherStatus(WeatherStatus.CLOUDY);
			}else if(randomNum < 0.8){
				((SimulationSettings) myAgent).setCurWeatherStatus(WeatherStatus.SUNNY);
			}else {
				((SimulationSettings) myAgent).setCurWeatherStatus(WeatherStatus.RAINY);
			}
		}else {
			if(randomNum < 0.5) {
				((SimulationSettings) myAgent).setCurWeatherStatus(WeatherStatus.RAINY);
			}else if(randomNum < 0.9){
				((SimulationSettings) myAgent).setCurWeatherStatus(WeatherStatus.CLOUDY);
			}else {
				((SimulationSettings) myAgent).setCurWeatherStatus(WeatherStatus.SUNNY);
			}
		}
		
		
	}
}
