package com.mzom.meteoroute;

import android.content.Context;

import com.mapbox.directions.service.models.Waypoint;

class RouteConstants {

    static String getRouteRequestURL(Waypoint origin, Waypoint destination, Context context) {

        return "https://api.mapbox.com/directions/v5/mapbox/driving/"
                + String.valueOf(origin.getLongitude()) + "%2C"
                + String.valueOf(origin.getLatitude()) + "%3B"
                + String.valueOf(destination.getLongitude()) + "%2C"
                + String.valueOf(destination.getLatitude()) + ".json?access_token="
                + context.getString(R.string.mapbox_api_key)
                + "&overview=full&geometries=geojson";


    }

    private RouteConstants(){

    }

}
