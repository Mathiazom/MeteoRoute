package com.mzom.meteoroute;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

class WeatherIcons {

    static Bitmap[] getAllIcons(Resources res){

        return new Bitmap[]{
                fromWeather(WeatherConstants.WEATHER_TYPE_CLEAR,res),
                fromWeather(WeatherConstants.WEATHER_TYPE_CLOUDS,res),
                fromWeather(WeatherConstants.WEATHER_TYPE_RAIN,res),
                fromWeather(WeatherConstants.WEATHER_TYPE_UNKNOWN,res)
        };

    }

    static Bitmap fromWeather(int weatherType, Resources res){
        return fromWeather(weatherType,false,res);
    }
    
    static Bitmap fromWeather(int weatherType, boolean asMarker, Resources res){

        if(asMarker){

            switch (weatherType){

                case WeatherConstants.WEATHER_TYPE_CLEAR:
                    return BitmapFactory.decodeResource(res,R.drawable.marker_forecast_sunny);

                case WeatherConstants.WEATHER_TYPE_CLOUDS:
                    return BitmapFactory.decodeResource(res,R.drawable.marker_forecast_cloudy);

                case WeatherConstants.WEATHER_TYPE_RAIN:
                    return BitmapFactory.decodeResource(res,R.drawable.marker_forecast_rainy);

                case WeatherConstants.WEATHER_TYPE_SNOW:
                    return BitmapFactory.decodeResource(res,R.drawable.marker_forecast_other);

                case WeatherConstants.WEATHER_TYPE_THUNDERSTORM:
                    return BitmapFactory.decodeResource(res,R.drawable.marker_forecast_other);

                case WeatherConstants.WEATHER_TYPE_DRIZZLE:
                    return BitmapFactory.decodeResource(res,R.drawable.marker_forecast_other);

                case WeatherConstants.WEATHER_TYPE_ATMOSPHERE:
                    return BitmapFactory.decodeResource(res,R.drawable.marker_forecast_other);

                case WeatherConstants.WEATHER_TYPE_UNKNOWN:
                    return BitmapFactory.decodeResource(res,R.drawable.marker_forecast_other);

                default:
                    return BitmapFactory.decodeResource(res,R.drawable.marker_forecast_other);

            }
        }
        
        switch (weatherType){

            case WeatherConstants.WEATHER_TYPE_CLEAR:
                return BitmapFactory.decodeResource(res,R.drawable.forecast_sunny);

            case WeatherConstants.WEATHER_TYPE_CLOUDS:
                return BitmapFactory.decodeResource(res,R.drawable.forecast_cloudy);

            case WeatherConstants.WEATHER_TYPE_RAIN:
                return BitmapFactory.decodeResource(res,R.drawable.forecast_rainy);

            case WeatherConstants.WEATHER_TYPE_SNOW:
                return BitmapFactory.decodeResource(res,R.drawable.forecast_other);

            case WeatherConstants.WEATHER_TYPE_THUNDERSTORM:
                return BitmapFactory.decodeResource(res,R.drawable.forecast_other);

            case WeatherConstants.WEATHER_TYPE_DRIZZLE:
                return BitmapFactory.decodeResource(res,R.drawable.forecast_other);

            case WeatherConstants.WEATHER_TYPE_ATMOSPHERE:
                return BitmapFactory.decodeResource(res,R.drawable.forecast_other);

            case WeatherConstants.WEATHER_TYPE_UNKNOWN:
                return BitmapFactory.decodeResource(res,R.drawable.forecast_other);

            default:
                return BitmapFactory.decodeResource(res,R.drawable.forecast_other);
            
        }
        
    }
    
}
