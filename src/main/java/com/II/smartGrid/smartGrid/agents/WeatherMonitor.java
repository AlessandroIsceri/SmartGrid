package com.II.smartGrid.smartGrid.agents;

import com.II.smartGrid.smartGrid.behaviours.UpdateWeather;

import jade.core.Agent;

public class WeatherMonitor extends Agent{
	
	public enum Status {SUNNY, RAINY, CLOUDY};
	private Status curStatus;
	
	
	@Override
    public void setup() {
        addBehaviour(new UpdateWeather(this, 10000)); //period: after how much time the weather changes
    }


	public Status getCurStatus() {
		return curStatus;
	}


	public void setCurStatus(Status curStatus) {
		this.curStatus = curStatus;
	}
	
	
	
}
