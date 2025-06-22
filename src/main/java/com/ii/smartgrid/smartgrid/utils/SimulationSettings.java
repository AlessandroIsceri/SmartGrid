package com.ii.smartgrid.smartgrid.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WindSpeedStatus;

import jade.core.Agent;
import jade.tools.DummyAgent.DummyAgent;

public class SimulationSettings extends CustomAgent{
	
	private int nTurns;
	private List<String> agentNames;
	private int weatherTurnDuration;
    private double[][] weatherTransitionProbabilities;
    private double[][] windSpeedTransitionProbabilities;

	@Override
    public void setup() {   
        this.nTurns = 1440 / (TimeUtils.getTurnDuration());
        this.curTurn = 0;
        
        agentNames = new ArrayList<String>();
		Object[] args = this.getArguments();
		for(int i = 0; i < args.length - 3; i++) {
			agentNames.add((String) args[i]);
		}
        String weatherDuration = (String) args[args.length - 3];
		double latitude = Double.parseDouble((String) args[args.length - 2]);
		double longitude = Double.parseDouble((String) args[args.length - 1]);
        weatherTurnDuration = TimeUtils.convertTimeToTurn(weatherDuration);
        curWeather = WeatherStatus.SUNNY;
        curWindSpeed = WindSpeedStatus.CALM;

        weatherTransitionProbabilities = WeatherUtil.getWeatherTransitionProbabilities(latitude, longitude);
        windSpeedTransitionProbabilities = WeatherUtil.getWindTransitionProbabilities(latitude, longitude);

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

    public void updateTurn() {
		if(((this.curTurn + 1) % weatherTurnDuration) == 0) {
			updateWeather();
            updateWindSpeed();
			this.log("Weather updated: " + this.curWeather);
		}
        this.curTurn++;
	}


    private void updateWindSpeed() {
        int curStateRow = curWindSpeed.ordinal();
		double sum = Math.random();
		for(int i = 0; i < WindSpeedStatus.values().length; i++){
            sum -= windSpeedTransitionProbabilities[curStateRow][i];
			if(sum < 0){
				curWindSpeed = WindSpeedStatus.values()[i];
				break;
			}
		}
    }

    private void updateWeather(){
        int curStateRow = curWeather.ordinal();
		double sum = Math.random();
		for(int i = 0; i < WeatherStatus.values().length; i++){
            sum -= weatherTransitionProbabilities[curStateRow][i];
			if(sum < 0){
				curWeather = WeatherStatus.values()[i];
				break;
			}
		}
    }

}