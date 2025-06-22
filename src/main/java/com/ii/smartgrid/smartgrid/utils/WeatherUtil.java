package com.ii.smartgrid.smartgrid.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.lang.acl.ACLMessage;

public class WeatherUtil {
    
    public enum WeatherStatus {SUNNY, CLOUDY, RAINY, SNOWY};
    public enum WindSpeedStatus {CALM, LIGHT_AIR, LIGHT_BREEZE, GENTLE_BREEZE, MODERATE_BREEZE, FRESH_BREEZE, STRONG_BREEZE, NEAR_GALE, GALE, STRONG_GALE, STORM, VIOLENT_STORM, HURRICANE}

    private static String getResultFromHTTPRequest(double latitude, double longitude, String requestedParameter){
        URL url;
        try {
            // https://archive-api.open-meteo.com/v1/era5?
            // latitude=52.52&
            // longitude=13.41&
            // start_date=2021-01-01&
            // end_date=2021-12-31&
            // hourly=weather_code OR wind_speed_10m

            Map<String, String> parameters = new HashMap<>();
            parameters.put("latitude", "" + latitude);
            parameters.put("longitude", "" + longitude);
            parameters.put("start_date", "2019-01-01");
            parameters.put("end_date", "2024-12-31");
            parameters.put("hourly", requestedParameter);

            
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            if(resultString.length() > 0){
                resultString = resultString.substring(0, resultString.length() - 1);
            }else{
                System.out.println("Error: an error occurred while creating url request for weather information.");
            }
            
            HttpURLConnection con;
            url = new URL("https://archive-api.open-meteo.com/v1/era5?" + resultString);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(resultString);
            out.flush();
            out.close();

            int status = con.getResponseCode();
            if(status != 200){
                return null;
            }

            BufferedReader in = new BufferedReader(
            new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            return content.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static double[][] getWeatherTransitionProbabilities(double latitude, double longitude){
        
        //weather_code
        String content = getResultFromHTTPRequest(latitude, longitude, "weather_code");
        if(content == null){
            System.out.println("Error: getResultFromHTTPRequest returned null");
            return null;
        }

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonObject = mapper.readValue(content, typeRef);
            Map<String, Object> jsonHourlyObject = mapper.convertValue(jsonObject.get("hourly"), typeRef);

            List<Integer> weatherCodes = mapper.convertValue(jsonHourlyObject.get("weather_code"), new TypeReference<List<Integer>>() { });
            int weatherStatesNumber = WeatherStatus.values().length;
            double[][] weatherTransitionCounts = new double[weatherStatesNumber][weatherStatesNumber];
            for(int i = 0; i < weatherCodes.size() - 1; i++){
                WeatherStatus w0 = getWeatherStatusFromWeatherCode(weatherCodes.get(i));
                WeatherStatus w1 = getWeatherStatusFromWeatherCode(weatherCodes.get(i + 1));
                weatherTransitionCounts[w0.ordinal()][w1.ordinal()]++;
            }
        
            for(int i = 0; i < weatherStatesNumber; i++){
                for(int j = 0; j < weatherStatesNumber; j++){
                    weatherTransitionCounts[i][j] /= weatherCodes.size();
                }
            }
        
            return weatherTransitionCounts;
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static double[][] getWindTransitionProbabilities(double latitude, double longitude){
        //wind_speed_10m
        String content = getResultFromHTTPRequest(latitude, longitude, "wind_speed_10m");
        if(content == null){
            System.out.println("Error: getResultFromHTTPRequest returned null");
            return null;
        }

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonObject;
        try {
            jsonObject = mapper.readValue(content, typeRef);
            Map<String, Object> jsonHourlyObject = mapper.convertValue(jsonObject.get("hourly"), typeRef);

                
            List<Double> windSpeed = mapper.convertValue(jsonHourlyObject.get("wind_speed_10m"), new TypeReference<List<Double>>() {});

            int windStatesNumber = WindSpeedStatus.values().length;
            double[][] windTransitionCounts = new double[windStatesNumber][windStatesNumber];
            for(int i = 0; i < windSpeed.size() - 1; i++){
                WindSpeedStatus w0 = getWindSpeedStatusFromWindSpeed(windSpeed.get(i));
                WindSpeedStatus w1 = getWindSpeedStatusFromWindSpeed(windSpeed.get(i + 1));
                windTransitionCounts[w0.ordinal()][w1.ordinal()]++;
            }
            
            for(int i = 0; i < windStatesNumber; i++){
                for(int j = 0; j < windStatesNumber; j++){
                    windTransitionCounts[i][j] /= windSpeed.size();
                }
            }

            return windTransitionCounts;
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public static WeatherStatus getWeatherStatusFromWeatherCode(int weatherCode){
        //0-1 SUNNY
        //2, 3, 45, 48 CLOUDY
        //51 53 55 56 57 66 67 80 81 82 61 63 65 95 96 99 RAINY
        //77, 85, 86, 71, 73, 75 SNOWY        
        switch(weatherCode){
            case 0:
            case 1:
                return WeatherStatus.SUNNY;
            case 2:
            case 3:
            case 45:
            case 48:
                return WeatherStatus.CLOUDY;
            case 51:
            case 53:
            case 55:
            case 56:
            case 57:
            case 61:
            case 63:
            case 65:
            case 66:
            case 67:
            case 80: 
            case 81:
            case 82:
            case 95:
            case 96: 
            case 99:
                return WeatherStatus.RAINY;
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                return WeatherStatus.SNOWY;
            default:
                System.out.println("WEATHER CODE "+ weatherCode + " NOT FOUND");
                return WeatherStatus.SUNNY;
        }
    }

    public static WindSpeedStatus getWindSpeedStatusFromWindSpeed(double windSpeed){
        // SCALA DI BEAUFORT
        // 0: CALM --> < 1 KM/H
        // 1: LIGHT AIR --> <6 KM/H 
        // 2: LIGHT BREEZE --> <12 KM/H
        // 3: GENTLE BREEZE --> <19 KM/H
        // 4: MODERATE BREEZE --> < 29 KM/H
        // 5: FRESH BREEZE --> <39 KM/H
        // 6: STRONG BREEZE --> <50 KM/H
        // 7: NEAR GALE --> <62 KM/H
        // 8: GALE --> <75 KM/H
        // 9: STRONG GALE --> <89 KM/H
        // 10: STORM --> <103 KM/H
        // 11: VIOLENT STORM--> <118 KM/H
        // 12: HURRICANE --> > 118 KM/H
        if(windSpeed < 0){
            System.out.println("Error: WindSpeed contains a negative value");
            return WindSpeedStatus.CALM;
        } else if(windSpeed < 1){
            return WindSpeedStatus.CALM;
        } else if(windSpeed < 6){
            return WindSpeedStatus.LIGHT_AIR;   
        } else if(windSpeed < 12){
            return WindSpeedStatus.LIGHT_BREEZE;
        } else if(windSpeed < 19){
            return WindSpeedStatus.GENTLE_BREEZE;
        } else if(windSpeed < 29){
            return WindSpeedStatus.MODERATE_BREEZE;
        } else if(windSpeed < 39){
            return WindSpeedStatus.FRESH_BREEZE;
        } else if(windSpeed < 50){
            return WindSpeedStatus.STRONG_BREEZE;
        } else if(windSpeed < 62){
            return WindSpeedStatus.NEAR_GALE;
        } else if(windSpeed < 75){
            return WindSpeedStatus.GALE;
        } else if(windSpeed < 89){
            return WindSpeedStatus.STRONG_GALE;
        } else if(windSpeed < 103){
            return WindSpeedStatus.STORM;
        } else if(windSpeed < 118){
            return WindSpeedStatus.VIOLENT_STORM;
        } else {
            return WindSpeedStatus.HURRICANE;
        }
    }
}