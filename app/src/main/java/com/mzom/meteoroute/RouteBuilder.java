package com.mzom.meteoroute;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

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

    long startTime;

    Waypoint origin;
    String originPlaceName;

    Waypoint destination;
    String destinationPlaceName;

    ArrayList<LatLng> coordinates;

    LineString lineString;

    double distance = -1;
    double duration = -1;

    ArrayList<WeatherNode> weatherNodes;

    ArrayList<Waypoint> weatherPoints;

    RouteBuilder(Context context){
        this.mContext = context;
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

    RouteBuilder setWeatherNodes(ArrayList<WeatherNode> weatherNodes){
        this.weatherNodes = weatherNodes;
        return this;
    }

    RouteBuilder withJson(String json) {
        this.coordinates = getCoordinates(json);
        this.lineString = getLineString(json);
        this.distance = getDistance(json);
        this.duration = getDuration(json);
        return this;
    }

    private ArrayList<LatLng> getCoordinates(String json) {

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

    private LineString getLineString(String json) {

        if (json == null) return null;

        try {

            final JSONObject routeObject = new JSONObject(json).getJSONArray("routes").getJSONObject(0);

            return LineString.fromJson(routeObject.getJSONObject("geometry").toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private double getDistance(String json) {

        if (json == null) return -1;

        try {

            final JSONObject routeObject = new JSONObject(json).getJSONArray("routes").getJSONObject(0);

            return routeObject.getDouble("distance");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;

    }

    private double getDuration(String json) {

        if (json == null) return -1;

        try {

            final JSONObject routeObject = new JSONObject(json).getJSONArray("routes").getJSONObject(0);

            return routeObject.getDouble("duration");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;

    }


    long benchStart = 0;

    private int requestTimeUsage = 0;

    private void createRouteWeatherNodes(Runnable whenFinished){

        weatherPoints = getWeatherPoints();

        weatherNodes = new ArrayList<>();

        benchStart = System.currentTimeMillis();

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

                            requestTimeUsage += System.currentTimeMillis()-benchStart;

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
        void onRouteBuilt(Route route);
    }

    // Builds route object from given values, returns null if any of the values are invalid
    @Nullable
    void build(RouteBuilderListener callback) {

        final RouteBuilder builder = this;

        createRouteWeatherNodes(() -> {
            if(allValuesAreValid()){
                Route route = new Route(builder);
                callback.onRouteBuilt(route);
            }
        });


    }

    private boolean allValuesAreValid(){

        return this.origin != null
                && this.originPlaceName != null
                && this.destination != null
                && this.destinationPlaceName != null
                && this.startTime > 0
                && this.coordinates != null
                && this.coordinates.size() > 0
                && this.lineString != null
                && this.distance > 0
                && this.duration > 0
                && this.weatherNodes != null
                && this.weatherNodes.size() > 0;

    }

}