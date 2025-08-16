package com.ii.smartgrid.smartgrid.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherUtil {

    public static final double[] windSpeedAvg = {0.5, 3.5, 9.0, 15.5, 24.0, 34.0, 44.5, 56.0, 68.5, 82.0, 96.0, 110.5, 125.0};
    public static final int[] cloudCoverageAvg = new int[WeatherStatus.values().length];
    public static List<String> sunriseHours = new ArrayList<>();
    public static List<String> sunsetHours = new ArrayList<>();

    private static String getResultFromHTTPRequest(double latitude, double longitude, String startDate, String endDate, String frequence, String requestedParameter) {
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("latitude", "" + latitude);
            parameters.put("longitude", "" + longitude);
            parameters.put("start_date", startDate);
            parameters.put("end_date", endDate);
            parameters.put(frequence, requestedParameter);

            // Set TimeZone
            String timeZone = TimeUtils.getTimeZone();
            parameters.put("timezone", timeZone);
            StringBuilder result = new StringBuilder();
            // Create the query
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                result.append("&");
            }

            String resultString = result.toString();

            if (!resultString.isEmpty()) {
                // Remove the last "&"
                resultString = resultString.substring(0, resultString.length() - 1);
            } else {
                System.out.println("Error: an error occurred while creating url request for weather information.");
            }

            HttpURLConnection con;
            URL url = new URL("https://archive-api.open-meteo.com/v1/era5?" + resultString);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(resultString);
            out.flush();
            out.close();

            int status = con.getResponseCode();
            if (status != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            // Read the API output
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double[][] getWeatherTransitionProbabilities(double latitude, double longitude) {

        // API call to get data
        String content = getResultFromHTTPRequest(latitude, longitude, "2019-01-01", "2024-12-31", "hourly", "weather_code,cloud_cover");
        if (content == null) {
            System.out.println("Error: getResultFromHTTPRequest returned null");
            return new double[0][0];
        }

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonObject = mapper.readValue(content, typeRef);
            Map<String, Object> jsonHourlyObject = mapper.convertValue(jsonObject.get("hourly"), typeRef);

            List<Integer> weatherCodes = mapper.convertValue(jsonHourlyObject.get("weather_code"), new TypeReference<List<Integer>>() {});
            List<Integer> cloudCoveragePercentages = mapper.convertValue(jsonHourlyObject.get("cloud_cover"), new TypeReference<List<Integer>>() {});

            List<Integer> cloudCoverageOkta = new ArrayList<>();
            for (Integer percentage : cloudCoveragePercentages) {
                int cloudOkta = (int) Math.round((double) percentage / 100 * 8);
                cloudCoverageOkta.add(cloudOkta);
            }

            // Compute transition matrix for weather status
            int weatherStatesNumber = WeatherStatus.values().length;
            double[][] weatherTransitionProbabilities = new double[weatherStatesNumber][weatherStatesNumber];
            int[] rowCount = new int[weatherStatesNumber];

            for (int i = 0; i < weatherCodes.size() - 1; i++) {
                WeatherStatus w0 = getWeatherStatusFromWeatherCode(weatherCodes.get(i));
                WeatherStatus w1 = getWeatherStatusFromWeatherCode(weatherCodes.get(i + 1));
                weatherTransitionProbabilities[w0.ordinal()][w1.ordinal()]++;
                rowCount[w0.ordinal()]++;
                cloudCoverageAvg[w0.ordinal()] += cloudCoverageOkta.get(i);
            }

            for (int i = 0; i < weatherStatesNumber; i++) {
                for (int j = 0; j < weatherStatesNumber; j++) {
                    if (rowCount[i] != 0) {
                        weatherTransitionProbabilities[i][j] /= rowCount[i];
                    } else {
                        weatherTransitionProbabilities[i][j] = 0.0;
                    }
                }
            }

            // Compute the cloud coverage corresponding to a weather status
            for (int i = 0; i < weatherStatesNumber; i++) {
                if (rowCount[i] != 0) {
                    cloudCoverageAvg[i] = Math.round((float) cloudCoverageAvg[i] / rowCount[i]);
                } else {
                    cloudCoverageAvg[i] = 0;
                }
            }

            return weatherTransitionProbabilities;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new double[0][0];
    }

    public static double[][] getWindTransitionProbabilities(double latitude, double longitude) {
        // API call to get data
        String content = getResultFromHTTPRequest(latitude, longitude, "2019-01-01", "2024-12-31", "hourly", "wind_speed_10m");
        if (content == null) {
            System.out.println("Error: getResultFromHTTPRequest returned null");
            return new double[0][0];
        }

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonObject;
        try {
            jsonObject = mapper.readValue(content, typeRef);
            Map<String, Object> jsonHourlyObject = mapper.convertValue(jsonObject.get("hourly"), typeRef);

            List<Double> windSpeed = mapper.convertValue(jsonHourlyObject.get("wind_speed_10m"), new TypeReference<List<Double>>() {});

            // Compute transition probability matrix
            int windStatesNumber = WindSpeedStatus.values().length;
            double[][] windTransitionProbabilities = new double[windStatesNumber][windStatesNumber];
            double[] rowCount = new double[windStatesNumber];
            for (int i = 0; i < windSpeed.size() - 1; i++) {
                WindSpeedStatus w0 = getWindSpeedStatusFromWindSpeed(windSpeed.get(i));
                WindSpeedStatus w1 = getWindSpeedStatusFromWindSpeed(windSpeed.get(i + 1));
                windTransitionProbabilities[w0.ordinal()][w1.ordinal()]++;
                rowCount[w0.ordinal()]++;
            }

            for (int i = 0; i < windStatesNumber; i++) {
                for (int j = 0; j < windStatesNumber; j++) {
                    if (rowCount[i] != 0) {
                        windTransitionProbabilities[i][j] /= rowCount[i];
                    } else {
                        windTransitionProbabilities[i][j] = 0.0;
                    }
                }
            }

            return windTransitionProbabilities;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return new double[0][0];
    }

    public static void setSunriseAndSunset(double latitude, double longitude) {
        // API call to get data
        String content = getResultFromHTTPRequest(latitude, longitude, "2023-01-01", "2024-12-31", "daily", "sunrise,sunset");
        if (content == null) {
            System.out.println("Error: getResultFromHTTPRequest returned null");
            return;
        }

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonObject;
        try {
            jsonObject = mapper.readValue(content, typeRef);
            Map<String, Object> jsonDailyObject = mapper.convertValue(jsonObject.get("daily"), typeRef);

            TypeReference<ArrayList<String>> typeRefArrayList = new TypeReference<ArrayList<String>>() {};

            sunriseHours = mapper.convertValue(jsonDailyObject.get("sunrise"), typeRefArrayList);
            sunsetHours = mapper.convertValue(jsonDailyObject.get("sunset"), typeRefArrayList);

            for (int i = 0; i < sunriseHours.size(); i++) {
                String newSunriseHour = sunriseHours.get(i);
                sunriseHours.set(i, newSunriseHour.substring(11));
                String newSunsetHour = sunsetHours.get(i);
                sunsetHours.set(i, newSunsetHour.substring(11));
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    public static WeatherStatus getWeatherStatusFromWeatherCode(int weatherCode) {
        // 0, 1 SUNNY
        // 2, 3, 45, 48 CLOUDY
        // 51, 53, 55, 56, 57, 66, 67, 80, 81, 82, 61, 63, 65, 95, 96, 99 RAINY
        // 77, 85, 86, 71, 73, 75 SNOWY
        switch (weatherCode) {
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
                System.out.println("Error: Weather code " + weatherCode + " not found");
                return WeatherStatus.SUNNY;
        }
    }

    public static WindSpeedStatus getWindSpeedStatusFromWindSpeed(double windSpeed) {
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
        if (windSpeed < 0) {
            System.out.println("Error: WindSpeed contains a negative value");
            return WindSpeedStatus.CALM;
        } else if (windSpeed < 1) {
            return WindSpeedStatus.CALM;
        } else if (windSpeed < 6) {
            return WindSpeedStatus.LIGHT_AIR;
        } else if (windSpeed < 12) {
            return WindSpeedStatus.LIGHT_BREEZE;
        } else if (windSpeed < 19) {
            return WindSpeedStatus.GENTLE_BREEZE;
        } else if (windSpeed < 29) {
            return WindSpeedStatus.MODERATE_BREEZE;
        } else if (windSpeed < 39) {
            return WindSpeedStatus.FRESH_BREEZE;
        } else if (windSpeed < 50) {
            return WindSpeedStatus.STRONG_BREEZE;
        } else if (windSpeed < 62) {
            return WindSpeedStatus.NEAR_GALE;
        } else if (windSpeed < 75) {
            return WindSpeedStatus.GALE;
        } else if (windSpeed < 89) {
            return WindSpeedStatus.STRONG_GALE;
        } else if (windSpeed < 103) {
            return WindSpeedStatus.STORM;
        } else if (windSpeed < 118) {
            return WindSpeedStatus.VIOLENT_STORM;
        } else {
            return WindSpeedStatus.HURRICANE;
        }
    }

    public enum WeatherStatus {SUNNY, CLOUDY, RAINY, SNOWY}

    public enum WindSpeedStatus {CALM, LIGHT_AIR, LIGHT_BREEZE, GENTLE_BREEZE, MODERATE_BREEZE, FRESH_BREEZE, STRONG_BREEZE, NEAR_GALE, GALE, STRONG_GALE, STORM, VIOLENT_STORM, HURRICANE}

}