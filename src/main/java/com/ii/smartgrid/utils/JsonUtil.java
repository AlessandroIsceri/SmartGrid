package com.ii.smartgrid.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonUtil {

    public static String CABLES_PATH;
    public static String GRIDS_PATH;
    public static String LOAD_MANAGERS_PATH;
    public static String SMART_BUILDINGS_PATH;
    public static String DIESEL_POWERPLANTS_PATH;
    public static String SOLAR_POWERPLANTS_PATH;
    public static String HYDRO_POWERPLANTS_PATH;
    public static String WIND_POWERPLANTS_PATH;
    public static String OWNERS_PATH;

    private static String BASE_PATH = "src/main/resources/scenarios/";

    private JsonUtil() {
    }


    public static void setUpScenario(String scenarioName) {
        BASE_PATH = BASE_PATH + scenarioName + "/";

        CABLES_PATH = BASE_PATH + "cables.json";
        GRIDS_PATH = BASE_PATH + "grids.json";
        LOAD_MANAGERS_PATH = BASE_PATH + "loadManagers.json";
        SMART_BUILDINGS_PATH = BASE_PATH + "smartBuildings.json";
        DIESEL_POWERPLANTS_PATH = BASE_PATH + "dieselPowerplants.json";
        SOLAR_POWERPLANTS_PATH = BASE_PATH + "solarPowerPlants.json";
        HYDRO_POWERPLANTS_PATH = BASE_PATH + "hydroPowerplants.json";
        WIND_POWERPLANTS_PATH = BASE_PATH + "windPowerPlants.json";
        OWNERS_PATH = BASE_PATH + "owners.json";
    }

    public static <T> T readJsonFile(String path, String key, Class<T> clazz) {
        File inputFile = new File(path);
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(inputFile);
            JsonNode valueNode = rootNode.get(key);
            return mapper.treeToValue(valueNode, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getAllAgentNames() {
        String[] pathsToSkip = {CABLES_PATH, OWNERS_PATH};
        List<String> agentNames = new ArrayList<>();
        File dir = new File(BASE_PATH);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                boolean skip = false;
                for (String pathToSkip : pathsToSkip) {
                    Path p1 = Paths.get(child.getPath()).normalize();
                    Path p2 = Paths.get(pathToSkip).normalize();
                    if (p1.equals(p2)) {
                        skip = true;
                    }
                }
                if (!skip) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
                    Map<String, Object> fileContent;
                    try {
                        fileContent = objectMapper.readValue(child, typeRef);
                        agentNames.addAll(fileContent.keySet());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("No files in folder: " + BASE_PATH);
        }
        return agentNames;
    }

}