package com.mzom.meteoroute;

import android.content.Context;

import com.mapbox.mapboxsdk.geometry.LatLng;

class ForecastConstants {

    static final int FORECAST_INTERVAL_MILLIS = 10800000;

    // Distance between points in meters
    static final int WEATHER_POINTS_INTERVAL = 50000;
    static final int MIN_WEATHER_POINTS = 5;
    static final int MAX_WEATHER_POINTS = 50;

    static final int FORECAST_PREVIEW_SIZE = 5;

    static String getForecastRequestURL(Context context, LatLng latLng) {

        return "http://api.openweathermap.org/data/2.5/forecast?lat="
                + String.valueOf(latLng.getLatitude()) + "&lon="
                + String.valueOf(latLng.getLongitude()) + "&APPID="
                + context.getString(R.string.open_weather_map_api_key);
    }

    private ForecastConstants(){

    }

}
