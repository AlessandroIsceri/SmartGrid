package com.ii.smartgrid.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EnergyMonitorUtil {
    private static List<Double> solarRenewableEnergyProduction = new ArrayList<>();
    private static List<Double> hydroRenewableEnergyProduction = new ArrayList<>();
    private static List<Double> windRenewableEnergyProduction = new ArrayList<>();
    private static List<Double> nonRenewableEnergyProduction = new ArrayList<>();
    private static List<Double> buildingEnergyProduction = new ArrayList<>(); 
    private static List<Double> totalEnergyDemand = new ArrayList<>();
    private static List<Double> storedBatteryEnergy = new ArrayList<>();
    private static List<Double> batteryProduction = new ArrayList<>();

    private static String BASE_PATH = "src/main/resources/output/";
    private static int firstTurn = 0;
    private static int lastTurn = 0;

    private static synchronized void addValue(List<Double> list, double value, int turn) {

        turn = turn % TimeUtils.getDailyTurnsNumber();
            
        // First value for current turn
        while (list.size() <= turn) {
            list.add(0.0);
        }
        
        // Sum the values of current turn
        list.set(turn, list.get(turn) + value);
    }

    public static void addSolarRenewableEnergyProduction(double value, int turn) {
        addValue(solarRenewableEnergyProduction, value, turn);
    }

    public static void addHydroRenewableEnergyProduction(double value, int turn) {
        addValue(hydroRenewableEnergyProduction, value, turn);
    }

    public static void addWindRenewableEnergyProduction(double value, int turn) {
        addValue(windRenewableEnergyProduction, value, turn);
    }

    public static void addNonRenewableEnergyProduction(double value, int turn) {
        addValue(nonRenewableEnergyProduction, value, turn);
    }

    public static void addTotalEnergyDemand(double value, int turn) {
        addValue(totalEnergyDemand, value, turn);
    }

    public static void addBuildingEnergyProduction(double value, int turn){
        addValue(buildingEnergyProduction, value, turn);
    }

    public static void addBatteryStoredEnergy(double value, int turn){
        addValue(storedBatteryEnergy, value, turn);
    }

    public static void addBatteryProduction(double value, int turn){
        addValue(batteryProduction, value, turn);
    }

    public static void saveData() {
        Map<String, List<Double>> content = new HashMap<>();
        content.put("solarRenewableEnergyProduction", solarRenewableEnergyProduction);
        content.put("hydroRenewableEnergyProduction", hydroRenewableEnergyProduction);
        content.put("windRenewableEnergyProduction", windRenewableEnergyProduction);
        content.put("nonRenewableEnergyProduction", nonRenewableEnergyProduction);
        content.put("buildingEnergyProduction", buildingEnergyProduction);
        content.put("totalEnergyDemand", totalEnergyDemand);
        content.put("storedBatteryEnergy", storedBatteryEnergy);
        content.put("batteryProduction", batteryProduction);

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

        // Empty the lists
        solarRenewableEnergyProduction.clear();
        hydroRenewableEnergyProduction.clear();
        windRenewableEnergyProduction.clear();
        nonRenewableEnergyProduction.clear();
        totalEnergyDemand.clear();
        storedBatteryEnergy.clear();
        buildingEnergyProduction.clear();
        batteryProduction.clear();
    }


    public static void setUpScenario(String scenarioName) {
        BASE_PATH = BASE_PATH + scenarioName + "/";
        lastTurn = TimeUtils.getDailyTurnsNumber() - 1;
    }


}
