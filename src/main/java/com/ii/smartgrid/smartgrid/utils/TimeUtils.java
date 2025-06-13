package com.ii.smartgrid.smartgrid.utils;

import java.io.FileInputStream;
import java.util.Properties;

public class TimeUtils {
    
	private static int turnDuration = -1;
	
	private static void initTurnDuration() {
		if(turnDuration == -1) {
			Properties prop = new Properties();
			String fileName = "src/main/resources/app.config";
			try (FileInputStream fis = new FileInputStream(fileName)) {
				prop.load(fis);
			} catch (Exception ex) {
				System.out.println("File app.config not found");
			}
			String _turnDuration = prop.getProperty("turn_duration");
			int[] tmp = TimeUtils.splitTime(_turnDuration);
	        int hours = tmp[0];
	        int minutes = tmp[1];
	        //24:00 = 1440 minutes
			turnDuration = hours * 60 + minutes;
		}
	}
	
	public static int getTurnDuration() {
		initTurnDuration();
		return turnDuration;
	}
	
    public static int[] splitTime(String time){
        String[] tmp = time.split(":");
        //returns the array: [hours, minutes]
        return new int[] {Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1])};
    }

	public static int convertTimeToTurn(String time) {
		initTurnDuration();
		int[] tmp = splitTime(time);
        int hours = tmp[0];
        int minutes = tmp[1];
        int timeMinutes = hours * 60 + minutes;
		// 12:30; 00:30 -> 25
        int turn = timeMinutes / turnDuration;
        return turn;
	}
	
	public static String convertTurnToTime(int turn){
		initTurnDuration();
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
	
	
}