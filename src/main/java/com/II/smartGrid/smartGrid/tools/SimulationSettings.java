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
		this.curTurn++;
		if(((this.curTurn) % weatherTurnDuration) == 0) {
			UpdateWeather updateWeather = new UpdateWeather(this);
			addBehaviour(updateWeather);
			while(updateWeather.done() == false);
			this.log("Weather updated: " + this.curWeatherStatus);
		}
	}
}
