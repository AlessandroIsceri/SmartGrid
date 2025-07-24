package com.ii.smartgrid.smartgrid.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.agents.CustomAgent;
import com.ii.smartgrid.smartgrid.model.Cable;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WindSpeedStatus;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.tools.DummyAgent.DummyAgent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class SimulationSettings extends CustomAgent{
	
    private final String CONFIG_PATH = "src/main/resources/app.config";
    // private final String PACKAGE_PATH = "com.ii.smartgrid.smartgrid.agents.";
    private final String PACKAGE_PATH = CustomAgent.class.getPackage().getName();


	private List<String> agentNames;
    private double[][] weatherTransitionProbabilities;
    private double[][] windSpeedTransitionProbabilities;
    enum SimulationStatus {ON, OFF};
    private SimulationStatus simulationStatus;
	

	@Override
    public void setup() {   
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            prop.load(fis);
        } catch (Exception ex) {
            System.out.println("File app.config not found");
        }
        String turnDurationStr = prop.getProperty("turn_duration");
        TimeUtils.computeAndSetTurnDuration(turnDurationStr);
        
        double latitude = Double.parseDouble(prop.getProperty("latitude"));
        double longitude = Double.parseDouble(prop.getProperty("longitude"));
                
        String weatherTurnDurationStr = prop.getProperty("weather_turn_duration");
        TimeUtils.computeAndSetWeatherTurnDuration(weatherTurnDurationStr);

        String scenarioName = prop.getProperty("scenario_name");
        JsonUtil.setUpScenario(scenarioName);
        
        this.curTurn = 0;
        agentNames = JsonUtil.getAllAgentNames();
        
        try {
            ContainerController conC = this.getContainerController();
            for(String agentName : agentNames){
                String className = agentName.split("-")[0]; 
                AgentController ac = conC.createNewAgent(agentName, PACKAGE_PATH + "." + className + "Agent", null);
                ac.start();
            }        
            
            //a inizio simulazione behaviour di customagent che manda mex a tutti i vicini e aspetta ogni risposta cosi da setuppare le coordinate dei cables
                
        }
        catch (StaleProxyException e) {
            e.printStackTrace();
        }

        curWeather = WeatherStatus.SUNNY;
        curWindSpeed = WindSpeedStatus.CALM;

        weatherTransitionProbabilities = WeatherUtil.getWeatherTransitionProbabilities(latitude, longitude);
        for(int i = 0; i < weatherTransitionProbabilities.length; i++){
            for(int j = 0; j < weatherTransitionProbabilities.length; j++){
                System.out.print(String.format("%.2f", weatherTransitionProbabilities[i][j]) + "\t");
            }
            System.out.println("");
        }
        
        windSpeedTransitionProbabilities = WeatherUtil.getWindTransitionProbabilities(latitude, longitude);
        for(int i = 0; i < windSpeedTransitionProbabilities.length; i++){
            for(int j = 0; j < windSpeedTransitionProbabilities.length; j++){
                System.out.print(String.format("%.2f", windSpeedTransitionProbabilities[i][j]) + "\t");
            }
            System.out.println("");
        }

        WeatherUtil.setSunriseAndSunset(latitude, longitude);

        this.log("Setup completed");
        addBehaviour(new StartNewTurn(this));  
        addBehaviour(new CheckSimulationSettingsMessages(this));  
	}

    void sendMessages() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Map<String, Object> content = new HashMap<String, Object>();
		content.put(MessageUtil.CURRENT_TURN, this.curTurn);
        content.put(MessageUtil.CURRENT_WEATHER, this.curWeather.ordinal());
        content.put(MessageUtil.CURRENT_WIND_SPEED, this.curWindSpeed.ordinal());

        this.log("Weather: " + this.curWeather);
        this.log("Wind speed: " + this.curWindSpeed);

		// List<String> allAgentNames = ((SimulationSettings) myAgent).getAgentNames();
		for(int i = 0; i < agentNames.size(); i++) {
            this.createAndSend(ACLMessage.INFORM, agentNames.get(i), content, "turn-" + agentNames.get(i));
		}
	}

	public List<String> getAgentNames() {
		return agentNames;
	}

	public int getCurTurn() {
		return curTurn;
	}

    public SimulationStatus getSimulationStatus() {
        return simulationStatus;
    }

    public void setSimulationStatus(SimulationStatus simulationStatus) {
        this.simulationStatus = simulationStatus;
    }

    public void updateTurn() {
		if(((this.curTurn + 1) % TimeUtils.getWeatherTurnDuration()) == 0) {
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