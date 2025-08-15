package com.ii.smartgrid.smartgrid.model;

public class Coordinates {
    private double latitude;
    private double longitude;

    public Coordinates() {
        super();
    }


    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getRadiansLatitude() {
        return Math.toRadians(latitude);
    }

    public double getRadiansLongitude() {
        return Math.toRadians(longitude);
    }

}
