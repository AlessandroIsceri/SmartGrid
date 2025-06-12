package com.II.smartGrid.smartGrid.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.II.smartGrid.smartGrid.agents.CustomAgent;
import com.II.smartGrid.smartGrid.model.TimeUtils;

import jade.core.Agent;
import jade.tools.DummyAgent.DummyAgent;

public class SimulationSettings extends CustomAgent{
	
	private int nTurns;
	private List<String> agentNames;
	public enum WeatherStatus {SUNNY, RAINY, CLOUDY};
	private WeatherStatus curWeatherStatus;
	private int weatherTurnDuration;

	@Override
    public void setup() {   
        this.nTurns = 1440 / (TimeUtils.getTurnDuration());
        this.curTurn = 0;
        
        agentNames = new ArrayList<String>();
		Object[] args = this.getArguments();
		for(int i = 0; i < args.length - 1; i++) {
			agentNames.add((String) args[i]);
		}
        String weatherDuration = (String) args[args.length - 1];
        weatherTurnDuration = TimeUtils.convertTimeToTurn(weatherDuration);
        curWeatherStatus = WeatherStatus.SUNNY;
        
        this.log("Setup completed");
        addBehaviour(new StartNewTurn(this));  
	}

	public List<String> getAgentNames() {
		return agentNames;
	}

	public int getnTurns() {
		return nTurns;
	}

	public int getCurTurn() {
		return curTurn;
	}
	
	public WeatherStatus getCurWeatherStatus() {
		return curWeatherStatus;
	}

	public void setCurWeatherStatus(WeatherStatus curWeatherStatus) {
		this.curWeatherStatus = curWeatherStatus;
	}

	public void updateTurn() {
		if(((this.curTurn + 1) % weatherTurnDuration) == 0) {
			updateWeather();
			this.log("Weather updated: " + this.curWeatherStatus);
		}
        this.curTurn++;
	}

    private void updateWeather(){
		double randomNum = Math.random(); 
		
		// Current state	Sunny (S)		Cloudy (C)		Rainy (R)
		// Sunny (S)		0.7				0.2				0.1
		// Cloudy (C)		0.3				0.5				0.2
		// Rainy (R)		0.1				0.4				0.5
		
		
		if(curWeatherStatus == WeatherStatus.SUNNY) {
			if(randomNum < 0.7) {
				this.curWeatherStatus = WeatherStatus.SUNNY;
			}else if(randomNum < 0.9){
				this.curWeatherStatus = WeatherStatus.CLOUDY;
			}else {
				this.curWeatherStatus = WeatherStatus.RAINY;
			}
		}else if(curWeatherStatus == WeatherStatus.CLOUDY) {
			if(randomNum < 0.5) {
				this.curWeatherStatus = WeatherStatus.CLOUDY;
			}else if(randomNum < 0.8){
				this.curWeatherStatus = WeatherStatus.SUNNY;
			}else {
				this.curWeatherStatus = WeatherStatus.RAINY;
			}
		}else {
			if(randomNum < 0.5) {
				this.curWeatherStatus = WeatherStatus.RAINY;
			}else if(randomNum < 0.9){
				this.curWeatherStatus = WeatherStatus.CLOUDY;
			}else {
				this.curWeatherStatus = WeatherStatus.SUNNY;
			}
		}
    }

}
