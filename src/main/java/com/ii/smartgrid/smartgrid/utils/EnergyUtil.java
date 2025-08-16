package com.ii.smartgrid.smartgrid.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.model.Cable;

public class EnergyUtil {

    private static final String CSV_PATH = "src/main/resources/meanElectricityPrice.csv";
    private static Map<String, Cable> cableTypes;
    private static List<Cable> links;
    private static double priceVolatility;
    private static double priceTrend;
    private static Random rand;

    // Initialize cables on first class call
    static {
        rand = new Random(42);
        loadCables();
    }

    private EnergyUtil() {
    }

    private static void loadCables() {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        Map<String, Object> fileContent;
        try {
            fileContent = objectMapper.readValue(new File(JsonUtil.CABLES_PATH), typeRef);

            TypeReference<HashMap<String, Cable>> typeRefCables = new TypeReference<HashMap<String, Cable>>() {};
            cableTypes = objectMapper.convertValue(fileContent.get("types"), typeRefCables);

            TypeReference<ArrayList<Cable>> typeRefArrayList = new TypeReference<ArrayList<Cable>>() {};
            links = objectMapper.convertValue(fileContent.get("links"), typeRefArrayList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCountryFromCoordinates(double lat, double lon) {
        try {
            String url = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat="
                    + lat + "&lon=" + lon + "&zoom=3&addressdetails=1";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "JavaApp");
            conn.setRequestProperty("Accept-Language", "en");


            conn.setDoOutput(true);

            int status = conn.getResponseCode();
            if (status != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonObject = null;
            try {
                jsonObject = objectMapper.readValue(content.toString(), typeRef);
                return (String) jsonObject.get("name");
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Sets the initial value of electricity price based on the country
    public static double getMeanElectricityPriceFromCoordinates(double latitude, double longitude) {
        String country = getCountryFromCoordinates(latitude, longitude);
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_PATH))) {
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                String curCountry = values[0].trim();
                if (curCountry.equals(country)) {
                    System.out.println("Local electricity price: " + values[1].trim());
                    return Double.parseDouble(values[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static double randomWalk(double curPrice) {
        double variation = rand.nextGaussian() * priceVolatility + priceTrend;
        return curPrice * (1 + variation);
    }

    public static Cable getCableTypeInfo(String from, String to) {
        for (Cable cable : links) {
            boolean eq1 = cable.getFrom().equals(from) && cable.getTo().equals(to);
            boolean eq2 = cable.getTo().equals(from) && cable.getFrom().equals(to);
            if (eq1 || eq2) {
                return cableTypes.get(cable.getCableType());
            }
        }
        return null;
    }


    public static void setPriceVolatility(double priceVolatility) {
        EnergyUtil.priceVolatility = priceVolatility;
    }

    public static void setPriceTrend(double priceTrend) {
        EnergyUtil.priceTrend = priceTrend;
    }

}
