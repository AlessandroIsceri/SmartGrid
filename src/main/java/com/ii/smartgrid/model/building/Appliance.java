package com.ii.smartgrid.model.building;

public class Appliance {
    private String name;
    private boolean alwaysOn;
    private boolean on;
    private double hourlyConsumption;


    private Appliance() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Appliance other = (Appliance) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (alwaysOn != other.alwaysOn)
            return false;
        if (on != other.on)
            return false;
        return Double.doubleToLongBits(hourlyConsumption) == Double.doubleToLongBits(other.hourlyConsumption);
    }

    public double getHourlyConsumption() {
        return hourlyConsumption;
    }

    public void setHourlyConsumption(double hourlyConsumption) {
        this.hourlyConsumption = hourlyConsumption;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (alwaysOn ? 1231 : 1237);
        result = prime * result + (on ? 1231 : 1237);
        long temp;
        temp = Double.doubleToLongBits(hourlyConsumption);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public boolean isAlwaysOn() {
        return alwaysOn;
    }

    public void setAlwaysOn(boolean alwaysOn) {
        this.alwaysOn = alwaysOn;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public String toString() {
        return "Appliance [name=" + name + ", alwaysOn=" + alwaysOn + ", on=" + on + ", hourlyConsumption="
                + hourlyConsumption + "]";
    }


}
