package com.ii.smartgrid.smartgrid.model;

import com.ii.smartgrid.smartgrid.utils.SimulationSettings.WeatherStatus;

public class PhotovoltaicSystem implements EnergyProducer{
	private double hourlyProductionSunny;
	private double hourlyProductionRainy;
	private double hourlyProductionCloudy;
	
	public PhotovoltaicSystem(){
		super();
	}

	public PhotovoltaicSystem(double hourlyProductionSunny, double hourlyProductionRainy, double hourlyProductionCloudy) {
		super();
		this.hourlyProductionSunny = hourlyProductionSunny;
		this.hourlyProductionRainy = hourlyProductionRainy;
		this.hourlyProductionCloudy = hourlyProductionCloudy;
	}

	public double getHourlyProduction(WeatherStatus weather, int hour) {
		//Night time from 20:00  to 6:00
		if(hour < 6 || hour > 19) {
			return 0;
		}
		switch(weather) {
			case SUNNY:
				return hourlyProductionSunny;
			case RAINY:
				return hourlyProductionRainy;
			case CLOUDY:
				return hourlyProductionCloudy;
			default:
				return 0;
		}
	}    

    public double getHourlyProductionSunny() {
        return hourlyProductionSunny;
    }

    public void setHourlyProductionSunny(double hourlyProductionSunny) {
        this.hourlyProductionSunny = hourlyProductionSunny;
    }

    public double getHourlyProductionRainy() {
        return hourlyProductionRainy;
    }

    public void setHourlyProductionRainy(double hourlyProductionRainy) {
        this.hourlyProductionRainy = hourlyProductionRainy;
    }

    public double getHourlyProductionCloudy() {
        return hourlyProductionCloudy;
    }

    public void setHourlyProductionCloudy(double hourlyProductionCloudy) {
        this.hourlyProductionCloudy = hourlyProductionCloudy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PhotovoltaicSystem other = (PhotovoltaicSystem) obj;
        if (Double.doubleToLongBits(hourlyProductionSunny) != Double.doubleToLongBits(other.hourlyProductionSunny))
            return false;
        if (Double.doubleToLongBits(hourlyProductionRainy) != Double.doubleToLongBits(other.hourlyProductionRainy))
            return false;
        if (Double.doubleToLongBits(hourlyProductionCloudy) != Double.doubleToLongBits(other.hourlyProductionCloudy))
            return false;
        return true;
    }

    @Override
	public String toString() {
		return "PhotovoltaicSystem [hourlyProductionSunny=" + hourlyProductionSunny + ", hourlyProductionRainy=" + hourlyProductionRainy
				+ ", hourlyProductionCloudy=" + hourlyProductionCloudy + "]";
	}
	
}
