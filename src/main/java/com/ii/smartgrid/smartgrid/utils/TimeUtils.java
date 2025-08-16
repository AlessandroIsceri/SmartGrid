package com.ii.smartgrid.smartgrid.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class TimeUtils {

    private static final int MINUTES_IN_A_DAY = 1440;

    private static int turnDuration;
    private static int weatherTurnDuration;
    private static long simulationStartDateInMillis;
    private static String timeZone;

    private TimeUtils() {

    }

    public static void computeAndSetTurnDuration(String newTurnDuration) {
        turnDuration = convertDurationInMinutes(newTurnDuration);
    }

    public static void computeAndSetWeatherTurnDuration(String newWeatherTurnDuration) {
        weatherTurnDuration = convertDurationInMinutes(newWeatherTurnDuration);
    }

    private static int convertDurationInMinutes(String duration) {
        int[] tmp = TimeUtils.splitTime(duration);
        int hours = tmp[0];
        int minutes = tmp[1];
        return hours * 60 + minutes;
    }

    public static int getDailyTurnsNumber() {
        return MINUTES_IN_A_DAY / turnDuration;
    }

    public static int getCurrentDayFromTurn(int curTurn) {
        return ((curTurn * turnDuration) / MINUTES_IN_A_DAY) + 1;
    }

    public static double getTurnDurationHours() {
        return turnDuration / 60.0;
    }

    public static LocalTime getLocalTimeFromString(String time) {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static LocalTime getLocalTimeFromTurn(int turn) {
        return LocalTime.parse(convertTurnToTime(turn), DateTimeFormatter.ofPattern("HH:mm"));
    }

    private static int[] splitTime(String time) {
        String[] tmp = time.split(":");
        // Returns the array: [hours, minutes]
        return new int[]{Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1])};
    }

    public static int convertTimeToTurn(String time) {
        int[] tmp = splitTime(time);
        int hours = tmp[0];
        int minutes = tmp[1];
        int timeMinutes = hours * 60 + minutes;
        return timeMinutes / turnDuration;
    }

    public static String convertTurnToTime(int turn) {
        turn = turn % TimeUtils.getDailyTurnsNumber();

        int turnInMinutes = getMinutesFromTurn(turn);
        int hours = turnInMinutes / 60;
        int minutes = turnInMinutes % 60;
        String formattedHours = "";
        String formattedMinutes = "";

        if (hours < 10) {
            formattedHours = "0";
        }
        formattedHours += hours;

        if (minutes < 10) {
            formattedMinutes = "0";
        }
        formattedMinutes += minutes;

        return formattedHours + ":" + formattedMinutes;
    }

    public static int getWeatherTurnDuration() {
        return weatherTurnDuration;
    }

    public static int getMinutesFromTurn(int turn) {
        return turn * turnDuration;
    }

    public static String getTimeZone() {
        return timeZone;
    }

    public static void setTimeZone(String newTimeZone) {
        timeZone = newTimeZone;
    }

    private static int getMillisFromTurn(int turn) {
        return getMinutesFromTurn(turn) * 60 * 1000;
    }

    public static int getTimeZoneOffset(int turn) {
        long millis = simulationStartDateInMillis + getMillisFromTurn(turn);
        int timeZoneOffset = TimeZone.getTimeZone(timeZone).getOffset(millis);
        return getHoursFromMillis(timeZoneOffset);
    }

    public static int getHoursFromMillis(int millis) {
        return millis / 1000 / 60 / 60;
    }

    public static void setSimulationStartDate(String simulationStartDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(simulationStartDate, formatter);
        simulationStartDateInMillis = localDate.atStartOfDay(ZoneId.of(timeZone)).toInstant().toEpochMilli();
    }

}