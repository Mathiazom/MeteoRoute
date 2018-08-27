package com.mzom.meteoroute;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.geojson.LineString;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;

class Route {

    // Time of departure given in ms since Jan 1. 1970
    private long startTime;

    private Waypoint origin;
    private String originPlaceName;

    private Waypoint destination;
    private String destinationPlaceName;

    // Points to represent the route itself
    private ArrayList<LatLng> coordinates;

    // Distance of route in meters
    private double distance = -1;

    // Duration of route in seconds
    private double duration = -1;

    private ArrayList<WeatherNode> weatherNodes;

    Route(@NonNull final RouteBuilder routeBuilder) {

        this.origin = routeBuilder.getOrigin();
        this.originPlaceName = routeBuilder.getOriginPlaceName();
        this.destination = routeBuilder.getDestination();
        this.destinationPlaceName = routeBuilder.getDestinationPlaceName();
        this.startTime = routeBuilder.getStartTime();
        this.coordinates = routeBuilder.getCoordinates();
        this.distance = routeBuilder.getDistance();
        this.duration = routeBuilder.getDuration();
        this.weatherNodes = routeBuilder.getWeatherNodes();

    }

    Waypoint getOrigin(){
        return this.origin;
    }

    Waypoint getDestination(){
        return this.destination;
    }

    String getOriginPlaceName(){
        return this.originPlaceName;
    }

    String getDestinationPlaceName(){
        return this.destinationPlaceName;
    }

    long getStartTime(){
        return this.startTime;
    }

    double getDistance(){
        return this.distance;
    }

    double getDuration(){
        return this.duration;
    }

    ArrayList<LatLng> getCoordinates(){
        return this.coordinates;
    }

    ArrayList<WeatherNode> getWeatherNodes(){
        return this.weatherNodes;
    }



}


