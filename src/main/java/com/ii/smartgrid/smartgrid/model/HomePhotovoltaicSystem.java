package com.ii.smartgrid.smartgrid.model;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;

public class HomePhotovoltaicSystem{
	
	public HomePhotovoltaicSystem(){
		super();
	}

    private double efficiency;
    private double area;
    private double azimuthAngleArray; //default 0 --> pannelli sud
    private double tiltAngle; // default 30

    private final double GLOBAL_SOLAR_CONSTANT = 1367.7; // W / m^2 
    private final double ALBEDO = 0.33; //red tiles
    private double latitude;
    private double longitude;


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getHourlyProduction(WeatherStatus curWeather, int curTurn) {
        int dayOfTheYear = TimeUtils.getCurrentDayFromTurn(curTurn);

        LocalTime curTime = TimeUtils.getLocalTimeFromTurn(curTurn);
		LocalTime sunriseTime = TimeUtils.getLocalTimeFromString(WeatherUtil.sunriseHours.get(dayOfTheYear));
        LocalTime sunsetTime = TimeUtils.getLocalTimeFromString(WeatherUtil.sunsetHours.get(dayOfTheYear));
        int curTimeInMinutes = TimeUtils.getMinutesFromTurn(curTurn);

        double STANDARD_MERIDIAN = TimeUtils.getTimeZoneOffset(curTurn) * 15; //degrees

        // At night solar energy production is 0
        if (curTime.isBefore(sunriseTime) || curTime.isAfter(sunsetTime)){
            return 0;
        }

        int cloudCover = WeatherUtil.cloudCoverageAvg[curWeather.ordinal()];
                
        // double declinationAngle = 23.45 * Math.sin( (360.0/365.0) * ((double)dayOfTheYear - 81.0));
        double declinationAngle = Math.toRadians(23.45 * Math.sin(Math.toRadians((360.0/365.0) * ((double)dayOfTheYear - 81.0))));

        // double longitudeInDegrees = this.coordinates.getLongitude();
        double latitudeInRadians = Math.toRadians(latitude);
        
        double x = Math.toRadians(360.0/365.0 * ((double) dayOfTheYear - 1.0));
        double equationOfTime = 9.87 * Math.sin(2.0*x) - 7.53 * Math.cos(x) - 1.5 * Math.sin(x);
    
        double solarTimeInMinutes = curTimeInMinutes + (STANDARD_MERIDIAN - longitude) * 4.0 + equationOfTime;
        double solarTimeInHours = solarTimeInMinutes / 60.0;

        double w = Math.toRadians((solarTimeInHours - 12.0) * 15.0); //degrees -> to radians

        //cos(z) = cosφ cosδ cosω + sinφ sin δ
        double cosZenithAngle = Math.cos(latitudeInRadians) * Math.cos(declinationAngle) * Math.cos(w) + Math.sin(latitudeInRadians) * Math.sin(declinationAngle); 

        double zenithAngle = Math.acos(cosZenithAngle);
        double sinZenithAngle = Math.sin(zenithAngle);
        
        double Izero = GLOBAL_SOLAR_CONSTANT * (1.0 + 0.033 * Math.cos(( 2.0 * Math.PI / 365.0) * (double)dayOfTheYear));
        double GHIClear = Izero * 0.7 * cosZenithAngle;
        
        double GHI = GHIClear * (1.0 - 0.75 * Math.pow(cloudCover, 3.4));
        double kT = GHI / (Izero * Math.max(0.065, cosZenithAngle));

        double DHI;
        if(kT > 0.8) {
            DHI = GHI * 0.165;
        } else if (kT >= 0.22){
            DHI = GHI * (0.951 - 0.16 * kT + 4.388 * Math.pow(kT, 2) - 16.64 * Math.pow(kT, 3) + 12.34 * Math.pow(kT, 4));            
        } else if(kT >= 0){
            DHI = (1.0 - 0.09 * kT) * GHI;
        } else {
            System.out.println("An error occurred while calculating kT in HomePhotovoltaic.");
            return -1; 
        }

        double DNI = (GHI - DHI) / cosZenithAngle;

        //ASSUMPTION: PV ARRAY directed at north, so the last piece of formula can be replaced with only cos of the azimuth since the azimutAngle of the array is 0.
        double cosAzimuth = (Math.sin(declinationAngle) * Math.cos(latitudeInRadians) - Math.cos(w) * Math.cos(declinationAngle) * Math.sin(latitudeInRadians)) / Math.sin(zenithAngle);
        
        double tiltAngleInRadians = Math.toRadians(tiltAngle);
        
        double incidenceCosine = cosZenithAngle * Math.cos(tiltAngleInRadians) + sinZenithAngle * Math.sin(tiltAngleInRadians) * cosAzimuth;

        if (incidenceCosine < -1.0){
            incidenceCosine = -1.0;
        } else if (incidenceCosine > 1.0) {
            incidenceCosine = 1.0;
        }

        double incidenceAngle = Math.acos(incidenceCosine);

        double GBeamPoa = DNI * Math.cos(incidenceAngle);
        double GDiffusePoa = DHI * ((1.0 + Math.cos(tiltAngleInRadians)) / 2.0);
        double GGroundPoa = GHI * ALBEDO * ((1.0 - Math.cos(tiltAngleInRadians)) / 2.0);

        double GPoa = GBeamPoa + GDiffusePoa + GGroundPoa;

        return efficiency * area * GPoa;
	}
    
	public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public double getAzimuthAngleArray() {
        return azimuthAngleArray;
    }

    public void setAzimuthAngleArray(double azimuthAngleArray) {
        this.azimuthAngleArray = azimuthAngleArray;
    }

    public double getTiltAngle() {
        return tiltAngle;
    }

    public void setTiltAngle(double tiltAngle) {
        this.tiltAngle = tiltAngle;
    }

    public double getLatitude() {
        return latitude;
    }

    
}
