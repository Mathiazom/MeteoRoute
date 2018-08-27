package com.mzom.meteoroute;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mzom.meteoroute.ForecastConstants.FORECAST_PREVIEW_SIZE;
import static com.mzom.meteoroute.ForecastConstants.MAX_WEATHER_POINTS;
import static com.mzom.meteoroute.ForecastConstants.MIN_WEATHER_POINTS;
import static com.mzom.meteoroute.ForecastConstants.WEATHER_POINTS_INTERVAL;

class RouteBuilder {

    private Context mContext;

    private LoadingManager loadingManager;

    private long startTime;

    private Waypoint origin;
    private String originPlaceName;

    private Waypoint destination;
    private String destinationPlaceName;

    private ArrayList<LatLng> coordinates;

    private LineString lineString;

    private double distance = -1;
    private double duration = -1;

    private ArrayList<WeatherNode> weatherNodes;

    private ArrayList<Waypoint> weatherPoints;

    RouteBuilder(Context context){

        this.mContext = context;

        try{
            loadingManager  = (LoadingManager) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement LoadingManager");
        }
    }

    RouteBuilder setOrigin(Waypoint origin, String originPlaceName) {
        this.origin = origin;
        this.originPlaceName = originPlaceName;
        return this;
    }

    RouteBuilder setDestination(Waypoint destination, String destinationPlaceName) {
        this.destination = destination;
        this.destinationPlaceName = destinationPlaceName;
        return this;
    }

    RouteBuilder setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    long getStartTime(){
        return this.startTime;
    }

    Waypoint getOrigin(){
        return this.origin;
    }

    String getOriginPlaceName(){
        return this.originPlaceName;
    }

    Waypoint getDestination(){
        return this.destination;
    }

    String getDestinationPlaceName(){
        return this.destinationPlaceName;
    }

    ArrayList<LatLng> getCoordinates(){
        return this.coordinates;
    }

    double getDistance() {
        return this.distance;
    }

    double getDuration() {
        return this.duration;
    }

    ArrayList<WeatherNode> getWeatherNodes() {
        return this.weatherNodes;
    }


    private void retrieveRouteDataFromJSON(String json) {
        this.coordinates = coordinatesFromJSON(json);
        this.lineString = lineStringFromJSON(json);
        this.distance = distanceFromJSON(json);
        this.duration = durationFromJSON(json);
    }

    private ArrayList<LatLng> coordinatesFromJSON(String json) {

        if (json == null) return null;

        try {

            final JSONObject routeObject = new JSONObject(json).getJSONArray("routes").getJSONObject(0);

            final ArrayList<LatLng> coordinates = new ArrayList<>();

            // Retrieve all route points as LatLng objects
            final JSONArray routeCoordinates = routeObject.getJSONObject("geometry").getJSONArray("coordinates");
            for (int i = 0; i < routeCoordinates.length(); i++) {
                coordinates.add(new LatLng(routeCoordinates.getJSONArray(i).getDouble(1), routeCoordinates.getJSONArray(i).getDouble(0)));
            }

            return coordinates;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private LineString lineStringFromJSON(String json) {

        if (json == null) return null;

        try {

            final JSONObject routeObject = new JSONObject(json).getJSONArray("routes").getJSONObject(0);

            return LineString.fromJson(routeObject.getJSONObject("geometry").toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private double distanceFromJSON(String json) {

        if (json == null) return -1;

        try {

            final JSONObject routeObject = new JSONObject(json).getJSONArray("routes").getJSONObject(0);

            return routeObject.getDouble("distance");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;

    }

    private double durationFromJSON(String json) {

        if (json == null) return -1;

        try {

            final JSONObject routeObject = new JSONObject(json).getJSONArray("routes").getJSONObject(0);

            return routeObject.getDouble("duration");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;

    }


    private void createRouteWeatherNodes(Runnable whenFinished){

        weatherPoints = getWeatherPoints();

        weatherNodes = new ArrayList<>();

        JSONRetriever.sendRouteRequest(mContext,origin, origin, handleWeatherNodeJSONResponse(origin, startTime,whenFinished));
    }

    private JSONRetriever.JSONRetrieverInterface handleWeatherNodeJSONResponse(final Waypoint waypoint, final long prevTime, final Runnable whenFinished){

        return json -> {

            try {

                if (json == null) return;

                double durationFromPrev = (new JSONObject(json).getJSONArray("routes").getJSONObject(0).getDouble("duration"));
                final long pointTime = prevTime + (int)(durationFromPrev*1000);

                final LatLng latLng = new LatLng(waypoint.getLatitude(), waypoint.getLongitude());

                JSONRetriever.sendForecastRequest(mContext,latLng, forecastJson -> {

                    try {

                        if (forecastJson == null) return;

                        final JSONObject forecast = new JSONObject(forecastJson);

                        // Get timeline of forecasts for this location
                        final JSONArray timeList = forecast.getJSONArray("list");

                        String placeName = "";

                        for (int i = 0; i < timeList.length(); i++) {

                            JSONObject dateObject = timeList.getJSONObject(i);
                            long timeLong = dateObject.getLong("dt") * 1000;

                            // Use this forecast if it is the closest in terms of ETA of this weather node
                            if (pointTime - timeLong < ForecastConstants.FORECAST_INTERVAL_MILLIS / 2 || timeLong > pointTime) {

                                // Weather object from JSON response
                                final JSONObject weatherObject = dateObject
                                        .getJSONArray("weather")
                                        .getJSONObject(0);

                                // Convert weather type string to a valid constant usable in this app
                                int weatherType = WeatherConstants.fromString(weatherObject.getString("main"));

                                // Full weather description
                                final String weatherDescription = weatherObject.getString("description");

                                // Weather node temperature
                                double temp = dateObject.getJSONObject("main").getDouble("temp");

                                // Check for rainfall
                                double rainFall = 0;
                                if (dateObject.has("rain")) {
                                    JSONObject rainObj = dateObject.getJSONObject("rain");
                                    if (rainObj != null && rainObj.has("3h")) {
                                        rainFall = rainObj.getDouble("3h");
                                    }
                                }

                                // Check for snowfall
                                double snowFall = 0;
                                if (dateObject.has("snow")) {
                                    JSONObject snowObj = dateObject.getJSONObject("snow");
                                    if (snowObj != null && snowObj.has("3h")) {
                                        snowFall = snowObj.getDouble("3h");
                                    }
                                }

                                // Registered location name for weather node
                                if (forecast.getJSONObject("city") != null && forecast.getJSONObject("city").has("name")) {
                                    placeName = forecast.getJSONObject("city").getString("name");
                                }

                                // Construct weather node from information gathered from JSON response
                                final WeatherNode weatherNode = new WeatherNode(latLng, placeName, pointTime, timeLong, weatherType, weatherDescription, temp, rainFall, snowFall);

                                // Store in weather nodes array for future reference
                                weatherNodes.add(weatherNode);

                                break;
                            }

                        }


                        if (weatherPoints.indexOf(waypoint) == weatherPoints.size() - 1) {

                            whenFinished.run();

                            return;
                        }


                        // Weather node created for this weather point, repeat process for next weather point

                        int nextIndex = weatherPoints.indexOf(waypoint) + 1;

                        final Waypoint nextWaypoint = weatherPoints.get(nextIndex);

                        JSONRetriever.sendRouteRequest(mContext,waypoint, nextWaypoint, handleWeatherNodeJSONResponse(nextWaypoint, pointTime, whenFinished));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                });


            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

    }

    private ArrayList<Waypoint> getWeatherPoints() {

        // Array for waypoints used in weather forecast
        ArrayList<Waypoint> weatherPoints = new ArrayList<>();

        int weatherPointsGap;

        double totalRouteDistance = distance;

        if (totalRouteDistance / WEATHER_POINTS_INTERVAL > MAX_WEATHER_POINTS) {
            weatherPointsGap = (int) totalRouteDistance / MAX_WEATHER_POINTS;
        } else if(totalRouteDistance / WEATHER_POINTS_INTERVAL < (Math.max(MIN_WEATHER_POINTS,FORECAST_PREVIEW_SIZE))) {
            weatherPointsGap = (int) totalRouteDistance / (Math.max(MIN_WEATHER_POINTS,FORECAST_PREVIEW_SIZE));
        }else{
            weatherPointsGap = WEATHER_POINTS_INTERVAL;
        }

        for (int i = 0; i < totalRouteDistance; i += weatherPointsGap) {

            // Get next point on route according to the specified interval
            Point p = TurfMeasurement.along(lineString, i, TurfConstants.UNIT_METERS);

            // Save measured point in array
            weatherPoints.add(new Waypoint(p.longitude(), p.latitude()));
        }

        // Always include last waypoint (destination waypoint)
        weatherPoints.add(new Waypoint(destination.getLongitude(), destination.getLatitude()));

        return weatherPoints;
    }


    interface RouteBuilderListener{
        void onRouteBuilt(@NonNull Route route);
        void onRouteBuildingFailed();
    }

    // Builds route object from given values, returns null if any of the values are invalid
    void build(@NonNull RouteBuilderListener callback) {

        loadingManager.setLoadingMessage("Validating user inputs");
        if(!RouteValuesValidator.validateInputs(origin,originPlaceName,destination,destinationPlaceName,startTime)){
            // Builder could not create route with the current data
            callback.onRouteBuildingFailed();
        }

        loadingManager.setLoadingMessage("Requesting route from inputs");
        JSONRetriever.sendRouteRequest(mContext, origin, destination, json ->  {

                loadingManager.setLoadingMessage("Retrieving request response");
                retrieveRouteDataFromJSON(json);

                loadingManager.setLoadingMessage("Creating weather nodes");
                createRouteWeatherNodes(() -> {

                    // Make sure builder is able to create a complete route
                    loadingManager.setLoadingMessage("Validating route data");
                    if(RouteValuesValidator.validateBuilder(this)){
                        loadingManager.setLoadingMessage("Finalizing route");
                        final Route route = new Route(this);
                        loadingManager.setLoadingMessage("Route built");
                        callback.onRouteBuilt(route);
                    }else{
                        // Builder could not create route with the current data
                        loadingManager.setLoadingMessage("Route building failed");
                        callback.onRouteBuildingFailed();
                    }
                });
        });




    }

}