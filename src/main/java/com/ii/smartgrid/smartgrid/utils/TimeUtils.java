package com.ii.smartgrid.smartgrid.utils;

import java.io.FileInputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import com.ii.smartgrid.smartgrid.agents.SmartHomeAgent;

public class TimeUtils {

    private static int MINUTES_IN_A_DAY = 1440;
	//private static int turnDuration = -1;

	// private static void initTurnDuration() {
	// 	if(turnDuration == -1) {
	// 		Properties prop = new Properties();
	// 		String fileName = "src/main/resources/app.config";
	// 		try (FileInputStream fis = new FileInputStream(fileName)) {
	// 			prop.load(fis);
	// 		} catch (Exception ex) {
	// 			System.out.println("File app.config not found");
	// 		}
	// 		String _turnDuration = prop.getProperty("turn_duration");
	// 		int[] tmp = TimeUtils.splitTime(_turnDuration);
	//         int hours = tmp[0];
	//         int minutes = tmp[1];
	//         //24:00 = 1440 minutes
	// 		turnDuration = hours * 60 + minutes;
	// 	}
	// }
	
	private static int turnDuration;
	private static int weatherTurnDuration;

	static void computeAndSetTurnDuration(String newTurnDuration){
        turnDuration = convertDurationInMinutes(newTurnDuration);
    }

	static void computeAndSetWeatherTurnDuration(String newWeatherTurnDuration){
        weatherTurnDuration = convertDurationInMinutes(newWeatherTurnDuration);
    }

	private static int convertDurationInMinutes(String duration){
        int[] tmp = TimeUtils.splitTime(duration);
        int hours = tmp[0];
        int minutes = tmp[1];
        //24:00 = 1440 minutes
        return hours * 60 + minutes;
    }

	public static int getDailyTurnsNumber(){
        return MINUTES_IN_A_DAY / turnDuration;
    }

	public static int getCurrentDayFromTurn(int curTurn){
        return ((curTurn * turnDuration) / 1440) + 1;
    }
    
	public static int getTurnDuration() {
		// initTurnDuration();
		return turnDuration;
	}

	public static int getTurnDurationHours(){
		return turnDuration / 60;
	}
	
	public static LocalTime getLocalTimeFromString(String time){
		return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
	}

	public static LocalTime getLocalTimeFromTurn(int turn){
		return LocalTime.parse(convertTurnToTime(turn), DateTimeFormatter.ofPattern("HH:mm"));
	}

    private static int[] splitTime(String time){
        String[] tmp = time.split(":");
        //returns the array: [hours, minutes]
        return new int[] {Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1])};
    }

	public static int convertTimeToTurn(String time) {
		// initTurnDuration();
		int[] tmp = splitTime(time);
        int hours = tmp[0];
        int minutes = tmp[1];
        int timeMinutes = hours * 60 + minutes;
		// 12:30; 00:30 -> 25
        int turn = timeMinutes / turnDuration;
        return turn;
	}
	
	public static String convertTurnToTime(int turn){

        turn = turn % TimeUtils.getDailyTurnsNumber();

		// initTurnDuration();
		int turnInMinutes = turn * turnDuration;
		int hours = turnInMinutes / 60;
		int minutes = turnInMinutes % 60;
		String formattedHours = "";
		String formattedMinutes = "";

		if(hours < 10) {
			formattedHours = "0";
		}
		formattedHours += hours;
		
		if(minutes < 10) {
			formattedMinutes = "0";
		}
		formattedMinutes += minutes;
		
		return formattedHours + ":" + formattedMinutes;
	}
	
    public static int getHourFromTurn(int turn){
        // initTurnDuration();
		int turnInMinutes = turn * turnDuration;
		int hours = turnInMinutes / 60;
        return hours;
    }

	public static int getWeatherTurnDuration() {
		return weatherTurnDuration;
	}
	
}