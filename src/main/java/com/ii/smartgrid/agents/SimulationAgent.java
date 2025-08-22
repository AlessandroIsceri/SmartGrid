package com.ii.smartgrid.agents;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import com.ii.smartgrid.behaviours.simulation.CheckSimulationMessagesBehaviour;
import com.ii.smartgrid.behaviours.simulation.StartNewTurnBehaviour;
import com.ii.smartgrid.utils.EnergyMonitorUtil;
import com.ii.smartgrid.utils.EnergyUtil;
import com.ii.smartgrid.utils.JsonUtil;
import com.ii.smartgrid.utils.MessageUtil;
import com.ii.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.utils.WeatherUtil;
import com.ii.smartgrid.utils.WeatherUtil.WeatherStatus;
import com.ii.smartgrid.utils.WeatherUtil.WindSpeedStatus;

import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import us.dustinj.timezonemap.TimeZone;
import us.dustinj.timezonemap.TimeZoneMap;

public class SimulationAgent extends CustomAgent{
	
    public enum SimulationStatus {ON, OFF}
    
    private static final String CONFIG_PATH = "src/main/resources/app.config";

	private List<String> agentNames;
    private double[][] weatherTransitionProbabilities;
    private double[][] windSpeedTransitionProbabilities;
    private SimulationStatus simulationStatus;
    private Random generator;
    private int intervalBetweenTurns; 
	

	@Override
    public void setup() {   
        generator = new Random(42);
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            prop.load(fis);
        } catch (Exception ex) {
            log("File app.config not found");
        }

        String turnDurationStr = prop.getProperty("turn_duration");
        TimeUtils.computeAndSetTurnDuration(turnDurationStr);
            
        intervalBetweenTurns = Integer.parseInt(prop.getProperty("interval_between_turns"));
        String saveInterval = prop.getProperty("save_interval");

        TimeUtils.computeAndSetSaveTurnDuration(saveInterval);

        double latitude = Double.parseDouble(prop.getProperty("latitude"));
        double longitude = Double.parseDouble(prop.getProperty("longitude"));
                
        TimeZoneMap map = TimeZoneMap.forEverywhere();
        TimeZone tz = map.getOverlappingTimeZone(latitude, longitude);
        String timeZone = tz.getZoneId();
        TimeUtils.setTimeZone(timeZone);

        String simulationStartDate = prop.getProperty("simulation_start_date");
        TimeUtils.setSimulationStartDate(simulationStartDate);

        String weatherTurnDurationStr = prop.getProperty("weather_turn_duration");
        TimeUtils.computeAndSetWeatherTurnDuration(weatherTurnDurationStr);


        String scenarioName = prop.getProperty("scenario_name");
        JsonUtil.setUpScenario(scenarioName);
        EnergyMonitorUtil.setUpScenario(scenarioName);
        
        this.curTurn = 0;
        agentNames = JsonUtil.getAllAgentNames();

        try {
            ContainerController conC = this.getContainerController();
            String packagePath = this.getClass().getPackage().getName();

            // Initialize every agent and populate the container
            for(String agentName : agentNames){
                String className = agentName.split("-")[0]; 
                Object[] params = null;
                AgentController ac = conC.createNewAgent(agentName, packagePath + "." + className + "Agent", params);
                ac.start();
            }                        
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
            System.out.println();
        }
        
        windSpeedTransitionProbabilities = WeatherUtil.getWindTransitionProbabilities(latitude, longitude);
        for(int i = 0; i < windSpeedTransitionProbabilities.length; i++){
            for(int j = 0; j < windSpeedTransitionProbabilities.length; j++){
                System.out.print(String.format("%.2f", windSpeedTransitionProbabilities[i][j]) + "\t");
            }
            System.out.println();
        }

        WeatherUtil.setSunriseAndSunset(latitude, longitude);

        double priceVolatility = Double.parseDouble(prop.getProperty("price_volatility"));
        double priceTrend = Double.parseDouble(prop.getProperty("price_trend"));

        EnergyUtil.setPriceVolatility(priceVolatility);
        EnergyUtil.setPriceTrend(priceTrend);
        
        curElectricityPrice = EnergyUtil.getMeanElectricityPriceFromCoordinates(latitude, longitude);

        addBehaviour(new StartNewTurnBehaviour(this));  
        addBehaviour(new CheckSimulationMessagesBehaviour(this));
        this.log("Setup completed");
	}

    public void sendMessages() {
		try {
			Thread.sleep(intervalBetweenTurns);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        // Prepares the messages for the next turn
		Map<String, Object> content = new HashMap<>();
        content.put(MessageUtil.CURRENT_TURN, this.curTurn);
        content.put(MessageUtil.CURRENT_WEATHER, this.curWeather.ordinal());
        content.put(MessageUtil.CURRENT_WIND_SPEED, this.curWindSpeed.ordinal());
        content.put(MessageUtil.ELECTRICITY_PRICE, this.curElectricityPrice);

        this.log("Weather: " + this.curWeather);
        this.log("Wind speed: " + this.curWindSpeed);

        // Send all the messages
		for(int i = 0; i < agentNames.size(); i++) {
            this.createAndSend(ACLMessage.INFORM, agentNames.get(i), content, "turn-" + agentNames.get(i));
		}
	}

	public List<String> getAgentNames() {
		return agentNames;
	}

    public SimulationStatus getSimulationStatus() {
        return simulationStatus;
    }

    public void setSimulationStatus(SimulationStatus simulationStatus) {
        this.simulationStatus = simulationStatus;
    }

    public void updateTurn() {
        if(((this.curTurn + 1) % TimeUtils.getSaveIntervalInTurns()) == 0){
            // Save monitor arrays
            EnergyMonitorUtil.saveData();
        }
            
        
		if(((this.curTurn + 1) % TimeUtils.getWeatherIntervalInTurns()) == 0) {
            // Update turn and weather
			updateWeather();
            updateWindSpeed();
			this.log("Weather updated: " + this.curWeather);
		}
        // Update electricity price
        curElectricityPrice = EnergyUtil.randomWalk(curElectricityPrice);
        this.curTurn++;
	}


    private void updateWindSpeed() {
        int curStateRow = curWindSpeed.ordinal();
		double sum = generator.nextDouble();
		for(int i = 0; i < WindSpeedStatus.values().length; i++){
            sum -= windSpeedTransitionProbabilities[curStateRow][i];
			if(sum < 0){
				curWindSpeed = WindSpeedStatus.values()[i];
                WeatherUtil.updateCurWindSpeed(i);
				break;
			}
		}
    }

    private void updateWeather(){
        int curStateRow = curWeather.ordinal();
		double sum = generator.nextDouble();
		for(int i = 0; i < WeatherStatus.values().length; i++){
            sum -= weatherTransitionProbabilities[curStateRow][i];
			if(sum < 0){
				curWeather = WeatherStatus.values()[i];
				break;
			}
		}
    }

}