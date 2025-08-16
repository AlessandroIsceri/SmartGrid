package com.ii.smartgrid.smartgrid.model.entities;

import java.time.LocalTime;

import com.ii.smartgrid.smartgrid.utils.TimeUtils;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil;
import com.ii.smartgrid.smartgrid.utils.WeatherUtil.WeatherStatus;

public class SolarPowerPlant extends RenewablePowerPlant {

    private static final double GLOBAL_SOLAR_CONSTANT = 1367.7; // W / m^2
    private static final double ALBEDO = 0.33; // Coefficient of the surface where panels are installed (red tiles)
    private double efficiency;
    private double area; // m^2
    private double azimuthAngleArray; 
    private double tiltAngle; // default 30

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

    @Override
    public double getHourlyProduction(Object... weatherConditions) {
        WeatherStatus curWeather = (WeatherStatus) weatherConditions[0];
        int curTurn = (int) weatherConditions[1];

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

        double longitudeInDegrees = this.coordinates.getLongitude();
        double latitudeInRadians = this.coordinates.getRadiansLatitude();

        double x = Math.toRadians(360.0 / 365.0 * (dayOfTheYear - 1.0));
        double equationOfTime = 9.87 * Math.sin(2.0 * x) - 7.53 * Math.cos(x) - 1.5 * Math.sin(x);

        double solarTimeInMinutes = curTimeInMinutes + (standardMeridian - longitudeInDegrees) * 4.0 + equationOfTime;
        double solarTimeInHours = solarTimeInMinutes / 60.0;

        double w = Math.toRadians((solarTimeInHours - 12.0) * 15.0); //degrees -> to radians

        double cosZenithAngle = Math.cos(latitudeInRadians) * Math.cos(declinationAngle) * Math.cos(w) + Math.sin(latitudeInRadians) * Math.sin(declinationAngle);

        double zenithAngle = Math.acos(cosZenithAngle);
        double sinZenithAngle = Math.sin(zenithAngle);

        double iZero = GLOBAL_SOLAR_CONSTANT * (1.0 + 0.033 * Math.cos((2.0 * Math.PI / 365.0) * dayOfTheYear));
        double ghiClear = iZero * 0.7 * cosZenithAngle;

        double ghi = ghiClear * (1.0 - 0.75 * Math.pow(cloudCover / 8.0, 3.4));
        double kT = ghi / (iZero * Math.max(0.065, cosZenithAngle));

        double dhi;
        if (kT > 0.8) {
            dhi = ghi * 0.165;
        } else if (kT >= 0.22) {
            dhi = ghi * (0.951 - 0.16 * kT + 4.388 * Math.pow(kT, 2) - 16.64 * Math.pow(kT, 3) + 12.34 * Math.pow(kT, 4));
        } else if (kT >= 0) {
            dhi = (1.0 - 0.09 * kT) * ghi;
        } else {
            System.out.println("An error occurred while calculating kT in SolarPowerPlant. " + kT);
            return 0;
        }

        double dni = (ghi - dhi) / cosZenithAngle;

        // ASSUMPTION: PV ARRAY directed at north, so the last piece of formula can be replaced with only cos of the azimuth since the azimutAngle of the array is 0.
        double cosAzimuth = (Math.sin(declinationAngle) * Math.cos(latitudeInRadians) - Math.cos(w) * Math.cos(declinationAngle) * Math.sin(latitudeInRadians)) / Math.sin(zenithAngle);

        double tiltAngleInRadians = Math.toRadians(tiltAngle);

        double incidenceCosine = cosZenithAngle * Math.cos(tiltAngleInRadians) + sinZenithAngle * Math.sin(tiltAngleInRadians) * cosAzimuth;

        if (incidenceCosine < -1.0) {
            incidenceCosine = -1.0;
        } else if (incidenceCosine > 1.0) {
            incidenceCosine = 1.0;
        }

        double incidenceAngle = Math.acos(incidenceCosine);

        double gBeamPoa = dni * Math.cos(incidenceAngle);
        double gDiffusePoa = dhi * ((1.0 + Math.cos(tiltAngleInRadians)) / 2.0);
        double gGroundPoa = ghi * ALBEDO * ((1.0 - Math.cos(tiltAngleInRadians)) / 2.0);

        double gPoa = gBeamPoa + gDiffusePoa + gGroundPoa;

        return efficiency * area * gPoa;
    }

    public double getTiltAngle() {
        return tiltAngle;
    }

    public void setTiltAngle(double tiltAngle) {
        this.tiltAngle = tiltAngle;
    }

}
