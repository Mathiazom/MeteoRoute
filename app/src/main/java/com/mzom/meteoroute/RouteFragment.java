package com.mzom.meteoroute;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapbox.android.gestures.StandardScaleGestureDetector;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mzom.meteoroute.ForecastConstants.FORECAST_PREVIEW_SIZE;

public class RouteFragment extends Fragment {

    private static final String TAG = "MRU-RouteFragment";

    private static final int CAMERA_ADJUST_ANIMATION_LENGTH = 500;

    // Array of icons used in forecast
    private Bitmap[] mWeatherIcons = null;

    private View view;

    private MapView mMapView;
    private MapboxMap mMapboxMap;

    // Initial zoom level adjusted to route
    private double adjustedMapZoom;

    // Keep track of which zoom levels have been adjusted for
    private int adjustedZoomLevel = -1;

    private MarkerOptions[] markerOptionsList;

    private Route mRoute;

    private RouteFragmentListener mCallback;

    interface RouteFragmentListener{

        void loadSearchFragmentForOrigin();

        void loadSearchFragmentForDestination();

        void flipRoute(Route route);

        void changeRouteStartTime(long startTime);

    }


    public static RouteFragment newInstance(Route route) {

        RouteFragment fragment = new RouteFragment();
        fragment.mRoute = route;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setRetainInstance(true);

        // Preload Bitmaps for weather nodes
        new WeatherIconsRetriever(weatherIcons -> mWeatherIcons = weatherIcons);

        view = inflater.inflate(R.layout.fragment_route,container,false);

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        initButtonListeners();

        loadRoute(mRoute);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mCallback = (RouteFragmentListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement RouteFragmentListener");
        }
    }

    private void initButtonListeners() {

        // Lets user change route start time
        view.findViewById(R.id.editStartTime).setOnClickListener(v -> newDatePicker(mRoute.getStartTime()));

        // Lets user change origin waypoint
        view.findViewById(R.id.editOrigin).setOnClickListener(v -> mCallback.loadSearchFragmentForOrigin());

        // Lets user change destination waypoint
        view.findViewById(R.id.editDestination).setOnClickListener(v -> mCallback.loadSearchFragmentForDestination());

        // Lets user flip route (i.e set destination waypoint as origin waypoint and vice versa)
        view.findViewById(R.id.flipRouteDirectionIcon).setOnClickListener(v -> mCallback.flipRoute(mRoute));

        // Lets user automatically apply correct zoom and rotation to the map camera such that the route is in focus
        view.findViewById(R.id.center_map_button).setOnClickListener(v -> adjustMapToRoute());

    }

    void loadRoute(Route route){

        // Hide route overview
        setRouteOverviewVisibility(false);

        mRoute = route;

        // Display user inputs (origin and destination)
        displayRouteInputs();

        mMapView.getMapAsync(mapboxMap -> {

            if(mapboxMap == null){

                Log.e(TAG,"Could not load map");

                return;
            }

            mMapboxMap = mapboxMap;

            // Remove any markers or polylines on map
            mMapboxMap.clear();

            drawRouteOnMap();

            displayWeatherMarkersOnMap();

            // Apply correct camera zoom and position to display route appropriately
            adjustMapToRoute();

            markRouteOriginOnMap(mRoute.getOrigin());
            markRouteDestinationOnMap(mRoute.getDestination());

            // Presents route duration and distance
            displayRouteDetails();

            // Presents a summary/preview of the route forecast
            displayRouteForecastPreview();

            setRouteOverviewVisibility(true);

        });

    }

    private void displayRouteInputs(){

        // Display route start time
        final TextView startTimeView = view.findViewById(R.id.editStartTime);
        if(mRoute.getStartTime() > 0){
            final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, d MMM yyyy", Locale.getDefault());
            startTimeView.setText(dateFormat.format(new Date(mRoute.getStartTime())));
        }

        // Display origin place name
        final TextView originPlaceNameView = view.findViewById(R.id.editOrigin);
        if(mRoute.getOriginPlaceName() == null){
            originPlaceNameView.setText("");
            return;
        }
        originPlaceNameView.setText(mRoute.getOriginPlaceName().trim());


        // Display destination place name
        final TextView destinationPlaceNameView = view.findViewById(R.id.editDestination);
        if(mRoute.getDestinationPlaceName() == null){
            destinationPlaceNameView.setText("");
            return;
        }
        destinationPlaceNameView.setText(mRoute.getDestinationPlaceName().trim());

    }

    private void newDatePicker(final long dateInMillis){

        // Use calendar object to get date info
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);

        // Get current route start date
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        final int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);

        if (getContext() == null) return;

        // Dialog to change route start date
        new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {

            // Set new values to calendar
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.set(year1, month1, dayOfMonth, hourOfDay, minute);

            // Get date from calendar and continue to time picker
            final long newDate = newCalendar.getTimeInMillis();
            newTimePicker(newDate);

        }, year, month, day).show();

    }

    private void newTimePicker(final long dateInMillis){

        // Get current route start time
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dateInMillis);
        final int hour = c.get(Calendar.HOUR_OF_DAY);
        final int minute = c.get(Calendar.MINUTE);
        final int year = c.get(Calendar.YEAR);
        final int month = c.get(Calendar.MONTH);
        final int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);

        if (getContext() == null) return;

        // Dialog to change route start time
        new TimePickerDialog(getContext(), (view, newHour, newMinute) -> {

            // Set new values to calendar
            Calendar newCalendar = Calendar.getInstance();
            newCalendar.set(year, month, dayOfMonth, newHour, newMinute);

            // Get date from calendar and notify activity with new start time
            final long newDate = newCalendar.getTimeInMillis();
            mCallback.changeRouteStartTime(newDate);

        }, hour, minute, true).show();

    }

    private void adjustMapToRoute() {

        //mapboxMap.getUiSettings().setAllGesturesEnabled(false);

        // Specify route bounds
        final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (LatLng l : mRoute.getCoordinates()) {
            boundsBuilder.include(l);
        }

        // Build bounds
        final LatLngBounds latLngBounds = boundsBuilder.build();

        // Apply bounds to map camera
        mMapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100, 650, 100, 600), CAMERA_ADJUST_ANIMATION_LENGTH, new MapboxMap.CancelableCallback() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onFinish() {

                adjustedMapZoom = mMapboxMap.getCameraPosition().zoom;

                adjustMarkersToZoomLevel((int)adjustedMapZoom);
            }
        });

    }

    // Use the route's coordinates to draw polyline representing the route
    private void drawRouteOnMap(){
        mMapboxMap.addPolyline(new PolylineOptions().addAll(mRoute.getCoordinates()).color(getResources().getColor(R.color.colorRoutePolyline)));
    }

    // Generate and place markers on map representing all weather nodes
    private void displayWeatherMarkersOnMap(){

        // Creating icon for marker requires a valid context used in IconFactory, so make sure it's not null
        if(getContext() == null){
            Log.e(TAG,"No context");
            return;
        }

        final ArrayList<WeatherNode> weatherNodes = mRoute.getWeatherNodes();
        Log.i(TAG,"Total weather nodes: " + weatherNodes.size());

        // Format to display time in local timezone
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm",Locale.getDefault());

        // MarkerOptions representing markers to be placed on map
        markerOptionsList = new MarkerOptions[weatherNodes.size()];

        final IconFactory iconFactory = IconFactory.getInstance(getContext());

        // Represent each weather node as marker on the map
        for(int w = 0;w<weatherNodes.size();w++){

            final WeatherNode weatherNode = weatherNodes.get(w);

            // String to be displayed on marker click
            final String markerTitle =
                    WeatherConstants.getStringFromConstant(weatherNode.getWeatherType())
                    + " (" + String.valueOf(weatherNode.getPlaceName() + ", "
                    + format.format(new Date(weatherNode.getTime()))) + ")";

            // Specify various marker attributes
            final MarkerOptions markerOptions = new MarkerOptions()
                    .position(weatherNode.getLatLng())
                    .setTitle(markerTitle);

            // All set, create icon for marker
            final Bitmap weatherBitmap = WeatherIcons.fromWeather(weatherNode.getWeatherType(),true,getResources());
            final Icon markerIcon = iconFactory.fromBitmap(weatherBitmap);
            markerOptions.setIcon(markerIcon);

            // Add to list of markers to be placed on map
            markerOptionsList[w] = markerOptions;

        }


        // Place all markers on map
        mMapboxMap.addMarkers(Arrays.asList(markerOptionsList));

        mMapboxMap.addOnScaleListener(onScaleListener);

    }

    // Listener triggered when map is scaled (zoomed in and out)
    private MapboxMap.OnScaleListener onScaleListener = new MapboxMap.OnScaleListener() {

        @Override
        public void onScaleBegin(@NonNull StandardScaleGestureDetector detector) {

        }

        @Override
        public void onScale(@NonNull StandardScaleGestureDetector detector) {

            int zoomLevel = (int) mMapboxMap.getCameraPosition().zoom;

            Log.i(TAG,"Zoom level");

            adjustMarkersToZoomLevel(zoomLevel);

        }

        @Override
        public void onScaleEnd(@NonNull StandardScaleGestureDetector detector) {

        }
    };

    // Create marker list to adjust number of map markers to the map's zoom level
    private void adjustMarkersToZoomLevel(int zoomLevel){

        // Zoom level has not changed, therefore markers will not be changed
        if(adjustedZoomLevel == zoomLevel){
            return;
        }

        // Register new zoom level
        adjustedZoomLevel = zoomLevel;

        // Zooming further than route level will not affect the markers
        if(zoomLevel > adjustedMapZoom+1){
            return;
        }

        // Remove all markers from map
        final List<Marker> currentMarkers = mMapboxMap.getMarkers();
        for(Marker marker : currentMarkers){
            mMapboxMap.removeMarker(marker);
        }

        // Calculate number of markers to be shown on this level
        float percentToShow = constraint((float)(zoomLevel)/(float)(adjustedMapZoom+2),0f,100f);
        int totalMarkersToShow = (int)(percentToShow*markerOptionsList.length);

        // Array used to fill markers to be shown on the map
        final MarkerOptions[] clonedMarkers = new MarkerOptions[markerOptionsList.length];
        Arrays.fill(clonedMarkers,null);

        // Avoid dividing by zero
        if(totalMarkersToShow > 0){

            // Use totalMarkerToShow to calculate an interval to add the correct number of markers
            for(int m = 0;m<clonedMarkers.length;m+=clonedMarkers.length/totalMarkersToShow){

                clonedMarkers[m] = markerOptionsList[m];

            }
        }

        // Convert array to list
        final ArrayList<MarkerOptions> toShow = new ArrayList<>();
        for(MarkerOptions markerOptions : clonedMarkers){
            if(markerOptions != null){
                toShow.add(markerOptions);
            }
        }


        // Add markers from zoom-adjusted list
        mMapboxMap.addMarkers(toShow);

    }

    private float constraint(float f, float min, float max) {
        return (f > max) ? max : (f < min ? min: f);
    }

    private void markRouteOriginOnMap(Waypoint originWaypoint){

        if(getContext() == null) return;

        final IconFactory iconFactory = IconFactory.getInstance(getContext());

        // Create and place marker for route origin
        final Icon originMarkerIcon = iconFactory.fromResource(R.drawable.blue_marker_filled);
        final MarkerOptions originMarkerOptions = new MarkerOptions()
                .position(new LatLng(originWaypoint.getLatitude(),originWaypoint.getLongitude()))
                .setIcon(originMarkerIcon);

        mMapboxMap.addMarker(originMarkerOptions);

    }

    private void markRouteDestinationOnMap(Waypoint destinationWaypoint){

        if(getContext() == null) return;

        final IconFactory iconFactory = IconFactory.getInstance(getContext());

        // Create and place marker for route destination
        final Icon destinationMarkerIcon = iconFactory.fromResource(R.drawable.red_marker_filled);
        final MarkerOptions destinationMarkerOptions = new MarkerOptions()
                .position(new LatLng(destinationWaypoint.getLatitude(),destinationWaypoint.getLongitude()))
                .setIcon(destinationMarkerIcon);

        mMapboxMap.addMarker(destinationMarkerOptions);

    }


    private void displayRouteDetails() {

        // Convert route duration to string with format #h #min
        int minutes = (int) mRoute.getDuration() / 60;
        int hours = minutes / 60;
        minutes -= hours * 60;

        String durationString = "";
        if(hours > 0){
            durationString += String.valueOf(hours) + " h ";
        }
        if(minutes > 0){
            durationString += String.valueOf(minutes) + " min";
        }

        // Display route duration in route details
        final TextView routeDurationText = view.findViewById(R.id.route_total_duration);
        routeDurationText.setText(durationString);


        // Convert route distance to string with format #.# km
        final String distanceString = "(" + new DecimalFormat(".#").format(mRoute.getDistance() / 1000) + " km)";

        // Display route distance in route details
        final TextView routeDistanceText = view.findViewById(R.id.route_total_distance);
        routeDistanceText.setText(distanceString);

    }

    private void displayRouteForecastPreview(){

        if(mWeatherIcons == null) mWeatherIcons = WeatherIcons.getAllIcons(getResources());


        final ArrayList<WeatherNode> weatherNodes = mRoute.getWeatherNodes();

        // Find max and min temperatures
        double[] temperatureBounds = WeatherNodesUtils.getTemperatureBounds(weatherNodes);
        double minTemp = temperatureBounds[0];
        double maxTemp = temperatureBounds[1];

        if(maxTemp != -1 && minTemp != -1){

            // Convert max and min temperatures from Kelvin to Celsius
            final int maxTempInt = (int)(TempConverter.kelvinToCelsius(maxTemp));
            final int minTempInt = (int)(TempConverter.kelvinToCelsius(minTemp));

            // Display max and min temperatures
            ((TextView) view.findViewById(R.id.max_min_temp_value)).setText(String.valueOf(minTempInt) + " / " + String.valueOf(maxTempInt));
        }

        /*// Find and display total route precipitation
        double totalPrecipitation = WeatherNodesUtils.getTotalPrecipitation(weatherNodes);
        Log.i(TAG,"Total precip: " + String.valueOf(totalPrecipitation));
        if(totalPrecipitation > 0){
            view.findViewById(R.id.precip_container).setVisibility(View.VISIBLE);
            ((TextView)view.findViewById(R.id.estimated_precip_value)).setText(String.valueOf(totalPrecipitation) + " mm");
        }else{
            view.findViewById(R.id.precip_container).setVisibility(View.GONE);
        }*/

        final LinearLayout forecastIconsContainer = view.findViewById(R.id.forecast_preview_icon_container);
        forecastIconsContainer.removeAllViews();

        final LinearLayout forecastTextsContainer = view.findViewById(R.id.forecast_preview_text_container);
        forecastTextsContainer.removeAllViews();

        final LayoutInflater inflater = LayoutInflater.from(getContext());

        // Pick and display weatherNodes for forecast preview
        for(int f = 0;f<FORECAST_PREVIEW_SIZE;f++){

            int index = (int)((weatherNodes.size()-1)*((float)f/FORECAST_PREVIEW_SIZE));

            final WeatherNode weatherNode = weatherNodes.get(index);

            final ImageView weatherIconView = (ImageView) inflater.inflate(R.layout.template_route_forecast_preview_icon, forecastIconsContainer,false);

            int weatherType = weatherNode.getWeatherType();

            Bitmap weatherIcon;
            if(weatherType > 2){
                weatherIcon = mWeatherIcons[3];
            }else{
                weatherIcon = mWeatherIcons[weatherType];
            }

            weatherIconView.setImageBitmap(weatherIcon);

            forecastIconsContainer.addView(weatherIconView);

            /*final SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());

            final TextView weatherTextView = (TextView) inflater.inflate(R.layout.template_route_forecast_preview_icon_text, forecastIconsContainer,false);
            weatherTextView.setText(format.format(new Date(weatherNode.getTime())));

            forecastTextsContainer.addView(weatherTextView);*/

            final LinearLayout weatherPathContainer;


            if(f == 0){
                weatherPathContainer = (LinearLayout)  inflater.inflate(R.layout.template_route_forecast_preview_path_origin,forecastTextsContainer,false);
            }else if(f == FORECAST_PREVIEW_SIZE-1){
                weatherPathContainer = (LinearLayout)  inflater.inflate(R.layout.template_route_forecast_preview_path_destination,forecastTextsContainer,false);
            }else{
                weatherPathContainer = (LinearLayout)  inflater.inflate(R.layout.template_route_forecast_preview_path_point,forecastTextsContainer,false);
            }

            forecastTextsContainer.addView(weatherPathContainer);


        }


    }


    private void setRouteOverviewVisibility(boolean visible){
        final ConstraintLayout routeDetailsContainer = view.findViewById(R.id.route_info_container);
        routeDetailsContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
