package com.II.smartGrid.smartGrid.behaviours;

import com.II.smartGrid.smartGrid.agents.WeatherMonitor;
import com.II.smartGrid.smartGrid.agents.WeatherMonitor.Status;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class UpdateWeather extends TickerBehaviour {

	private WeatherMonitor weatherMonitor; 
	
	public UpdateWeather(WeatherMonitor weatherMonitor, long period) {
		super(weatherMonitor, period);
		this.weatherMonitor = weatherMonitor;
	}

	@Override
	protected void onTick() {
		double randomNum = Math.random(); 
		Status curState = weatherMonitor.getCurStatus();
		
		// Current state	Sunny (S)		Cloudy (C)		Rainy (R)
		// Sunny (S)		0.7				0.2				0.1
		// Cloudy (C)		0.3				0.5				0.2
		// Rainy (R)		0.1				0.4				0.5
		
		
		if(curState == Status.SUNNY) {
			if(randomNum < 0.7) {
				weatherMonitor.setCurStatus(Status.SUNNY);
			}else if(randomNum < 0.9){
				weatherMonitor.setCurStatus(Status.CLOUDY);
			}else {
				weatherMonitor.setCurStatus(Status.RAINY);
			}
		}else if(curState == Status.CLOUDY) {
			if(randomNum < 0.5) {
				weatherMonitor.setCurStatus(Status.CLOUDY);
			}else if(randomNum < 0.8){
				weatherMonitor.setCurStatus(Status.SUNNY);
			}else {
				weatherMonitor.setCurStatus(Status.RAINY);
			}
		}else {
			if(randomNum < 0.5) {
				weatherMonitor.setCurStatus(Status.RAINY);
			}else if(randomNum < 0.9){
				weatherMonitor.setCurStatus(Status.CLOUDY);
			}else {
				weatherMonitor.setCurStatus(Status.SUNNY);
			}
		}
		
		
	}
}
