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

import jade.core.Agent;
import jade.tools.DummyAgent.DummyAgent;

public class SimulationSettings extends CustomAgent{
	
	private int nTurns;
	private List<String> agentNames;
	public enum WeatherStatus {SUNNY, RAINY, CLOUDY};
    public enum WindSpeedStatus {CALM, MODERATE_BREEZE, GALE, HURRICANE};
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
        curWeather = WeatherStatus.SUNNY;
        curWindSpeed = WindSpeedStatus.MODERATE_BREEZE;
        
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
        double randomNum = Math.random(); 
		// Current state	    CALM (C)		MODERATE_BREEZE (M)		GALE (G)    HURRICANE(H)
		// CALM (S)		            0.6	        0.4	                    0.0	        0.0
		// MODERATE_BREEZE (C)		0.2	        0.5	                    0.3	        0.0
		// GALE (R)		            0.0			0.3				        0.4         0.3
        // HURRICANE (H)            0.0         0.1                     0.4 	    0.5
		// TODO: CERCA DATI AFFIDABILI
		if(curWindSpeed == WindSpeedStatus.CALM) {
			if(randomNum < 0.6) {
				this.curWindSpeed = WindSpeedStatus.CALM;
			}else{
				this.curWindSpeed = WindSpeedStatus.MODERATE_BREEZE;
			}
		}else if(curWindSpeed == WindSpeedStatus.MODERATE_BREEZE) {
			if(randomNum < 0.5) {
				this.curWindSpeed = WindSpeedStatus.MODERATE_BREEZE;
			}else if(randomNum < 0.8){
				this.curWindSpeed = WindSpeedStatus.GALE;
			}else{
				this.curWindSpeed = WindSpeedStatus.CALM;
			}
		}else if(curWindSpeed == WindSpeedStatus.GALE){
			if(randomNum < 0.4) {
				this.curWindSpeed = WindSpeedStatus.GALE;
			}else if(randomNum < 0.7){
				this.curWindSpeed = WindSpeedStatus.HURRICANE;
			}else {
				this.curWindSpeed = WindSpeedStatus.MODERATE_BREEZE;
			}
		}else{
            if(randomNum < 0.5) {
				this.curWindSpeed = WindSpeedStatus.HURRICANE;
			}else if(randomNum < 0.9){
				this.curWindSpeed = WindSpeedStatus.GALE;
			}else {
				this.curWindSpeed = WindSpeedStatus.MODERATE_BREEZE;
			}
        }
    }

    private void updateWeather(){
		double randomNum = Math.random(); 
		
		// Current state	Sunny (S)		Cloudy (C)		Rainy (R)
		// Sunny (S)		0.7				0.2				0.1
		// Cloudy (C)		0.3				0.5				0.2
		// Rainy (R)		0.1				0.4				0.5
		// TODO: CERCA DATI AFFIDABILI

		
		if(curWeather == WeatherStatus.SUNNY) {
			if(randomNum < 0.7) {
				this.curWeather = WeatherStatus.SUNNY;
			}else if(randomNum < 0.9){
				this.curWeather = WeatherStatus.CLOUDY;
			}else {
				this.curWeather = WeatherStatus.RAINY;
			}
		}else if(curWeather == WeatherStatus.CLOUDY) {
			if(randomNum < 0.5) {
				this.curWeather = WeatherStatus.CLOUDY;
			}else if(randomNum < 0.8){
				this.curWeather = WeatherStatus.SUNNY;
			}else {
				this.curWeather = WeatherStatus.RAINY;
			}
		}else {
			if(randomNum < 0.5) {
				this.curWeather = WeatherStatus.RAINY;
			}else if(randomNum < 0.9){
				this.curWeather = WeatherStatus.CLOUDY;
			}else {
				this.curWeather = WeatherStatus.SUNNY;
			}
		}
    }

}
