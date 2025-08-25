package com.ii.smartgrid.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EnergyMonitorUtil {
    private static List<Double> photovoltaicProduction = new ArrayList<>();
    private static List<Double> hydroProduction = new ArrayList<>();
    private static List<Double> windProduction = new ArrayList<>();
    private static List<Double> dieselProduction = new ArrayList<>();
    private static List<Double> buildingProduction = new ArrayList<>(); 
    private static List<Double> totalDemand = new ArrayList<>();
    private static List<Double> storedBattery = new ArrayList<>();
    private static List<Double> batteryProduction = new ArrayList<>();
    private static List<Double> weatherState = new ArrayList<>();
    private static List<Double> windSpeed = new ArrayList<>();


    private static String BASE_PATH = "src/main/resources/output/";
    private static int firstTurn = 0;
    private static int lastTurn = 0;

    private static synchronized void addValue(List<Double> list, double value, int turn) {
        if(turn == firstTurn){
            turn = 0;
        }
        else if(turn == lastTurn){
            turn = TimeUtils.getDailyTurnsNumber();
        } else{
            turn = turn - (TimeUtils.getCurrentDayFromTurn(turn) - 1) * TimeUtils.getDailyTurnsNumber();
            turn = turn % (TimeUtils.getDailyTurnsNumber() + 1);
        }

        // First value for current turn
        while (list.size() <= turn) {
            list.add(0.0);
        }
        
        // Sum the values of current turn
        list.set(turn, list.get(turn) + value);
    }

    public static void addPhotovoltaicProduction(double value, int turn) {
        addValue(photovoltaicProduction, value, turn);
    }

    public static void addHydroProduction(double value, int turn) {
        addValue(hydroProduction, value, turn);
    }

    public static void addWindProduction(double value, int turn) {
        addValue(windProduction, value, turn);
    }

    public static void addDieselProduction(double value, int turn) {
        addValue(dieselProduction, value, turn);
    }

    public static void addTotalDemand(double value, int turn) {
        addValue(totalDemand, value, turn);
    }

    public static void addBuildingProduction(double value, int turn){
        addValue(buildingProduction, value, turn);
    }

    public static void addBatteryStored(double value, int turn){
        addValue(storedBattery, value, turn);
    }

    public static void addBatteryProduction(double value, int turn){
        addValue(batteryProduction, value, turn);
    }

    public static void addWindSpeed(double value, int turn){
        addValue(windSpeed, value, turn);
    }

    public static void addWeather(double value, int turn){
        addValue(weatherState, value, turn);
    }

    public static void saveData() {

        System.out.println("Saving energy data from turn " + firstTurn + " to turn " + lastTurn);

        Map<String, List<Double>> content = new HashMap<>();
        content.put("BatteryProduction", batteryProduction);
        content.put("BuildingProduction", buildingProduction);
        content.put("DieselProduction", dieselProduction);
        content.put("HydroProduction", hydroProduction);
        content.put("PhotovoltaicProduction", photovoltaicProduction);
        content.put("WindProduction", windProduction);
        content.put("TotalDemand", totalDemand);
        content.put("WindSpeed", windSpeed);
        content.put("WeatherState", weatherState);
        content.put("StoredBattery", storedBattery);


        ObjectMapper mapper = new ObjectMapper();
        String fileName = "energy_data-" + firstTurn + "-" + lastTurn + ".json";
        File file = new File(BASE_PATH + fileName);
        
        // Create directories if they don't exist
        File directoryPath = file.getParentFile();
        if (directoryPath != null && !directoryPath.exists()) {
            directoryPath.mkdirs();
        }
        // Write simulation data on JSON file
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        firstTurn += TimeUtils.getDailyTurnsNumber();
        lastTurn += TimeUtils.getDailyTurnsNumber();

        // Empty the lists but keep the last value
        photovoltaicProduction.subList(0, photovoltaicProduction.size() - 1).clear();
        hydroProduction.subList(0, hydroProduction.size() - 1).clear();
        windProduction.subList(0, windProduction.size() - 1).clear();
        dieselProduction.subList(0, dieselProduction.size() - 1).clear();
        totalDemand.subList(0, totalDemand.size() - 1).clear();
        storedBattery.subList(0, storedBattery.size() - 1).clear();
        buildingProduction.subList(0, buildingProduction.size() - 1).clear();
        batteryProduction.subList(0, batteryProduction.size() - 1).clear();
        weatherState.subList(0,weatherState.size() - 1).clear();
        windSpeed.subList(0, windSpeed.size() - 1).clear();
    }


    public static void setUpScenario(String scenarioName) {
        BASE_PATH = BASE_PATH + scenarioName + "/";
        lastTurn = TimeUtils.getDailyTurnsNumber();
    }


}
