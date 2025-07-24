package com.ii.smartgrid.smartgrid.model;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.ii.smartgrid.smartgrid.model.Battery;
import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;

public class SolarPowerPlant extends RenewablePowerPlant{
 
    private double efficiency;
    private double area;
    private double azimuthAngleArray; //default 0 --> pannelli sud
    private double tiltAngle; // default 30

    private final double GLOBAL_SOLAR_CONSTANT = 1367.7; // W / m^2 
    private final double ALBEDO = 0.33; //red tiles


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



    @Override
    public double getHourlyProduction(Object... weatherConditions) {
        WeatherStatus curWeather = (WeatherStatus) weatherConditions[0];
        int curTurn = (int) weatherConditions[1];
        
        int dayOfTheYear = TimeUtils.getCurrentDayFromTurn(curTurn);


        LocalTime curTime = TimeUtils.getLocalTimeFromTurn(curTurn);
		LocalTime sunriseTime = TimeUtils.getLocalTimeFromString(WeatherUtil.sunriseHours.get(dayOfTheYear));
        LocalTime sunsetTime = TimeUtils.getLocalTimeFromString(WeatherUtil.sunsetHours.get(dayOfTheYear));

        // At night solar energy production is 0
        if (curTime.isBefore(sunriseTime) || curTime.isAfter(sunsetTime)){
            return 0;
        }

        int cloudCover = WeatherUtil.cloudCoverageAvg[curWeather.ordinal()];
                
        double declination_angle = 23.45 * Math.sin( (360/365) * (dayOfTheYear - 81));
        double latitude = this.coordinates.getLatitude();
        double zenithAngle = latitude - declination_angle;
        
        double Izero = GLOBAL_SOLAR_CONSTANT * (1 + 0.033 * Math.cos(( 2 * Math.PI / 365) * dayOfTheYear));
        double GHI_clear = Izero * 0.7 * Math.cos(zenithAngle);
        
        double GHI = GHI_clear * (1 - 0.75 * Math.pow(cloudCover, 3.4));
        double kT = GHI / (Izero * Math.max(0.065, Math.cos(zenithAngle)));

        double DHI;
        if(kT > 0.8) {
            DHI = GHI * 0.165;
        } else if (kT >= 0.22){
            DHI = GHI * (0.951 - 0.16 * kT + 4.388 * Math.pow(kT, 2) - 16.64 * Math.pow(kT, 3) + 12.34 * Math.pow(kT, 4));            
        } else {
            DHI = (1 - 0.09 * kT) * GHI;
        }

        double DNI = (GHI - DHI) / Math.cos(zenithAngle);

        //ASSUMPTION: PV ARRAY directed at south, so the last piece of formula can be replaced with only cos of the azimuth since the azimutAngle of the array is 0.
        double cosAzimuth = Math.sin(zenithAngle) * Math.sin(latitude) - Math.sin(declination_angle) / (Math.cos(zenithAngle) * Math.cos(latitude));

        double incidenceAngle = 1 / Math.cos(Math.cos(zenithAngle) * Math.cos(tiltAngle) + Math.sin(zenithAngle) * Math.sin(tiltAngle) * cosAzimuth);
        
        double GBeamPoa = DNI * Math.cos(incidenceAngle);
        double GDiffusePoa = DHI * ((1 + Math.cos(tiltAngle)) / 2);
        double GGroundPoa = GHI * ALBEDO * ((1 - Math.cos(tiltAngle)) / 2);

        double GPoa = GBeamPoa + GDiffusePoa + GGroundPoa;
        return efficiency * area * GPoa;
	}   

}
