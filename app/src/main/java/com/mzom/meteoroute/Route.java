package com.mzom.meteoroute;

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

    private LineString lineString;

    // Distance of route in meters
    private double distance = -1;

    // Duration of route in seconds
    private double duration = -1;

    private ArrayList<WeatherNode> weatherNodes;

    Route(final RouteBuilder routeBuilder) {

        this.origin = routeBuilder.origin;
        this.originPlaceName = routeBuilder.originPlaceName;
        this.destination = routeBuilder.destination;
        this.destinationPlaceName = routeBuilder.destinationPlaceName;
        this.startTime = routeBuilder.startTime;
        this.coordinates = routeBuilder.coordinates;
        this.lineString = routeBuilder.lineString;
        this.distance = routeBuilder.distance;
        this.duration = routeBuilder.duration;
        this.weatherNodes = routeBuilder.weatherNodes;

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

    LineString getLineString(){
        return this.lineString;
    }

    ArrayList<WeatherNode> getWeatherNodes(){
        return this.weatherNodes;
    }



}


