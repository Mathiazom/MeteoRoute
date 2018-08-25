package com.mzom.meteoroute;

import com.mapbox.mapboxsdk.geometry.LatLng;

class WeatherNode {

    private LatLng latLng;

    private String placeName;

    private long time;

    private long forecastTime;

    private int weatherType;

    private String weatherDescription;

    private double temperature;

    private double rainFall;

    private double snowFall;

    WeatherNode(LatLng latLng, String placeName, long time, long forecastTime, int weatherType, String weatherDescription, double temperature, double rainFall, double snowFall) {

        this.latLng = latLng;

        this.placeName = placeName;

        this.time = time;

        this.forecastTime = forecastTime;

        this.weatherType = weatherType;

        this.weatherDescription = weatherDescription;

        this.temperature = temperature;

        this.rainFall = rainFall;

        this.snowFall = snowFall;

    }

    LatLng getLatLng() {
        return this.latLng;
    }

    String getPlaceName(){
        return this.placeName;
    }

    long getTime(){
        return this.time;
    }

    long getForecastTime(){
        return this.forecastTime;
    }

    int getWeatherType() {
        return this.weatherType;
    }

    String getWeatherDescription() {
        return this.weatherDescription;
    }

    double getTemperature() {
        return this.temperature;
    }

    double getRainFall() {
        return this.rainFall;
    }

    double getSnowFall() {
        return this.snowFall;
    }


}
