package com.mzom.meteoroute;

import android.graphics.BitmapFactory;
import android.graphics.Color;

class WeatherConstants {

    static final int WEATHER_TYPE_CLEAR = 0;
    static final int WEATHER_TYPE_CLOUDS = 1;
    static final int WEATHER_TYPE_RAIN = 2;
    static final int WEATHER_TYPE_SNOW = 3;
    static final int WEATHER_TYPE_THUNDERSTORM = 4;
    static final int WEATHER_TYPE_DRIZZLE = 5;
    static final int WEATHER_TYPE_ATMOSPHERE = 6;

    static final int WEATHER_TYPE_UNKNOWN = -1;

    static int fromString(String weatherString){

        switch(weatherString){

            case "Clear":
                return WEATHER_TYPE_CLEAR;
            case "Clouds":
                return WEATHER_TYPE_CLOUDS;
            case "Rain":
                return WEATHER_TYPE_RAIN;
            case "Snow":
                return WEATHER_TYPE_SNOW;
            case "Thunderstorm":
                return WEATHER_TYPE_THUNDERSTORM;
            case "Drizzle":
                return WEATHER_TYPE_DRIZZLE;
            case "Atmosphere":
                return WEATHER_TYPE_ATMOSPHERE;

            default:
                return WEATHER_TYPE_UNKNOWN;

        }


    }

    static String getStringFromConstant(int weatherConstant){

        switch (weatherConstant){

            case WeatherConstants.WEATHER_TYPE_CLEAR:
                return "Clear";

            case WeatherConstants.WEATHER_TYPE_CLOUDS:
                return "Clouds";

            case WeatherConstants.WEATHER_TYPE_RAIN:
                return "Rain";

            case WeatherConstants.WEATHER_TYPE_SNOW:
                return "Snow";

            case WeatherConstants.WEATHER_TYPE_THUNDERSTORM:
                return "Thunderstorm";

            case WeatherConstants.WEATHER_TYPE_DRIZZLE:
                return "Drizzle";

            case WeatherConstants.WEATHER_TYPE_ATMOSPHERE:
                return "Atmosphere";

            default:
                return "Unknown";

        }

    }

    static int getColorFromConstant(int weatherConstant){

        switch (weatherConstant){

            case WeatherConstants.WEATHER_TYPE_CLEAR:
                return Color.parseColor("#ffc107");

            case WeatherConstants.WEATHER_TYPE_CLOUDS:
                return Color.parseColor("#bbdefb");

            case WeatherConstants.WEATHER_TYPE_RAIN:
                return Color.parseColor("#3f51b5");

            case WeatherConstants.WEATHER_TYPE_SNOW:
                return Color.parseColor("#f5f5f5");

            case WeatherConstants.WEATHER_TYPE_THUNDERSTORM:
                return Color.parseColor("#455a64");

            case WeatherConstants.WEATHER_TYPE_DRIZZLE:
                return Color.parseColor("#80cbc4");

            case WeatherConstants.WEATHER_TYPE_ATMOSPHERE:
                return Color.parseColor("#a1887f");

            default:
                return Color.parseColor("#212121");

        }


    }


}
