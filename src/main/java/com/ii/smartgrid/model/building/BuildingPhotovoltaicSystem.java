package com.ii.smartgrid.model.building;

import java.time.LocalTime;

import com.ii.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.utils.WeatherUtil;
import com.ii.smartgrid.utils.WeatherUtil.WeatherStatus;

public class BuildingPhotovoltaicSystem {

    private static final double GLOBAL_SOLAR_CONSTANT = 1367.7; // W / m^2
    private static final double ALBEDO = 0.33; // Coefficient of the surface where panels are installed (red tiles)
    private double efficiency; 
    private double area; // m^2
    private double azimuthAngleArray; // Default 0
    private double tiltAngle; // Default 30
    private double latitude;
    private double longitude;

    public BuildingPhotovoltaicSystem() {
        super();
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

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public double getHourlyProduction(WeatherStatus curWeather, int curTurn) {
        int dayOfTheYear = TimeUtils.getCurrentDayFromTurn(curTurn);

        LocalTime curTime = TimeUtils.getLocalTimeFromTurn(curTurn);
        LocalTime sunriseTime = TimeUtils.getLocalTimeFromString(WeatherUtil.sunriseHours.get(dayOfTheYear));
        LocalTime sunsetTime = TimeUtils.getLocalTimeFromString(WeatherUtil.sunsetHours.get(dayOfTheYear));
        int curTimeInMinutes = TimeUtils.getMinutesFromTurn(curTurn);

        double standardMeridian = TimeUtils.getTimeZoneOffset(curTurn) * 15.0; //degrees

        // At night solar energy production is 0
        if (curTime.isBefore(sunriseTime) || curTime.isAfter(sunsetTime)) {
            return 0;
        }

        int cloudCover = WeatherUtil.cloudCoverageAvg[curWeather.ordinal()];
        
        double declinationAngle = Math.toRadians(23.45 * Math.sin(Math.toRadians((360.0 / 365.0) * (dayOfTheYear - 81.0))));
        double latitudeInRadians = Math.toRadians(latitude);

        double x = Math.toRadians(360.0 / 365.0 * (dayOfTheYear - 81.0));
        double equationOfTime = 9.87 * Math.sin(2.0 * x) - 7.53 * Math.cos(x) - 1.5 * Math.sin(x);

        double solarTimeInMinutes = curTimeInMinutes + (standardMeridian - longitude) * 4.0 + equationOfTime;
        double solarTimeInHours = solarTimeInMinutes / 60.0;

        double hourAngle = Math.toRadians((solarTimeInHours - 12.0) * 15.0); //degrees -> to radians

        double cosZenithAngle = Math.cos(latitudeInRadians) * Math.cos(declinationAngle) * Math.cos(hourAngle) + Math.sin(latitudeInRadians) * Math.sin(declinationAngle);

        double zenithAngle = Math.acos(cosZenithAngle);
        double sinZenithAngle = Math.sin(zenithAngle);

        double iZero = GLOBAL_SOLAR_CONSTANT * (1.0 + 0.033 * Math.cos((2.0 * Math.PI / 365.0) * dayOfTheYear));
        double ghiClear = iZero * 0.7 * cosZenithAngle;

        double ghi = ghiClear * (1.0 - 0.75 * Math.pow(cloudCover / 8.0, 3.4));
        double kt = ghi / (iZero * Math.max(0.065, cosZenithAngle));

        double dhi;
        if (kt > 0.8) {
            dhi = ghi * 0.165;
        } else if (kt >= 0.22) {
            dhi = ghi * (0.951 - 0.16 * kt + 4.388 * Math.pow(kt, 2) - 16.64 * Math.pow(kt, 3) + 12.34 * Math.pow(kt, 4));
        } else if (kt >= 0) {
            dhi = (1.0 - 0.09 * kt) * ghi;
        } else {
            System.out.println("An error occurred while calculating Kt in BuildingPhotovoltaic. " + kt);
            return 0;
        }

        double dni = (ghi - dhi) / cosZenithAngle;

        // ASSUMPTION: PV ARRAY directed at north, so the last piece of formula can be replaced with only cos of the azimuth since the azimutAngle of the array is 0.
        double cosAzimuthAngle = (Math.sin(declinationAngle) * Math.cos(latitudeInRadians) - Math.cos(hourAngle) * Math.cos(declinationAngle) * Math.sin(latitudeInRadians)) / Math.sin(zenithAngle);

        double tiltAngleInRadians = Math.toRadians(tiltAngle);

        double cosIncidenceAngle = cosZenithAngle * Math.cos(tiltAngleInRadians) + sinZenithAngle * Math.sin(tiltAngleInRadians) * cosAzimuthAngle;

        if (cosIncidenceAngle < -1.0) {
            cosIncidenceAngle = -1.0;
        } else if (cosIncidenceAngle > 1.0) {
            cosIncidenceAngle = 1.0;
        }

        double incidenceAngle = Math.acos(cosIncidenceAngle);

        double eBeamPoa = dni * Math.cos(incidenceAngle);
        double eDiffusePoa = dhi * ((1.0 + Math.cos(tiltAngleInRadians)) / 2.0);
        double eGroundPoa = ghi * ALBEDO * ((1.0 - Math.cos(tiltAngleInRadians)) / 2.0);

        double ePoa = eBeamPoa + eDiffusePoa + eGroundPoa;

        return efficiency * area * ePoa;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getTiltAngle() {
        return tiltAngle;
    }

    public void setTiltAngle(double tiltAngle) {
        this.tiltAngle = tiltAngle;
    }


}
