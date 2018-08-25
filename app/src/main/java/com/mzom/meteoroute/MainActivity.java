package com.mzom.meteoroute;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.TransitionSet;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.mapboxsdk.Mapbox;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements FragmentLoader, StartFragment.LoadSearchFromStartCallback, RouteFragment.RouteFragmentListener {

    private static final String TAG = "MRU-MainActivity";


    private StartFragment mStartFragment;

    private SearchFragment mSearchFragment;

    private RouteFragment mRouteFragment;

    private long startTime;

    private Waypoint origin;
    private String originPlaceName;

    private Waypoint destination;
    private String destinationPlaceName;

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationManager mLocationManager;

    private Location mCurrentLocation;


    private static final long LOCATION_REFRESH_TIME = 60000;
    private static final float LOCATION_REFRESH_DISTANCE = 20;


    private final LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(final Location location) {

            mCurrentLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_api_key));

        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        requestLocationUpdates();

        loadStartFragment();

    }

    private void requestLocationUpdates() {

        if (mLocationManager != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Get permissions it not granted
                requestRequiredPermissions();

                return;

            }

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            mCurrentLocation = location;

                            startTime = Calendar.getInstance().getTimeInMillis();
                            origin = new Waypoint(location.getLongitude(),location.getLatitude());
                            originPlaceName = "Your location";
                            /*destination = new Waypoint(6.149482,62.472229);
                            destinationPlaceName = "Ålesund";

                            buildRoute(origin, originPlaceName, destination, destinationPlaceName, startTime, new OnRouteBuiltListener() {
                                @Override
                                public void onRouteBuilt(Route route) {
                                    if(mRouteFragment == null) loadRouteFragment(route);
                                }

                                @Override
                                public void onRouteBuildFailed() {
                                    Log.e(TAG,"Could not build route");
                                }
                            });*/
                        }
                    });

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
        }
    }

    private static final int PERMISSION_REQUEST_CODE = 1600;

    private void requestRequiredPermissions() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {

            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {
                    Log.i(TAG, "Some permissions denied");
                    return;
                }
            }

            requestLocationUpdates();


        }

    }

    private void loadLoadingFragment(){

        /*final FrameLayout overlayFrameLayout = findViewById(R.id.overlay_frame_layout);
        final ConstraintLayout overlayLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.module_overlay_building_route,overlayFrameLayout,false);
        overlayFrameLayout.addView(overlayLayout);*/

        final LoadingFragment loadingFragment = new LoadingFragment();

        // Specify fragment transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            loadingFragment.setSharedElementEnterTransition(new TransitionSet().addTransition(new ChangeBounds()).
                    addTransition(new ChangeTransform()).
                    addTransition(new ChangeImageTransform()));

            loadingFragment.setEnterTransition(new Fade());
        }

        // Actual fragment replacement
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, loadingFragment)
                .addToBackStack(SearchFragment.class.getSimpleName())
                .commit();

        Log.i(TAG, "Loaded search fragment from start fragment");
    }

    @Override
    public void loadStartFragment() {

        mStartFragment = new StartFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_layout, mStartFragment)
                .addToBackStack(StartFragment.class.getSimpleName())
                .commit();
    }

    @Override
    public void loadSearchFragmentFromStart() {

        // Create fragment to search for route destination
        mSearchFragment = SearchFragment.newInstance((waypoint, placeName) -> buildRoute(origin, originPlaceName, waypoint, placeName, startTime, new OnRouteBuiltListener() {
            @Override
            public void onRouteBuilt(Route route) {
                loadRouteFragment(route);
                hideSearchFragment();
            }

            @Override
            public void onRouteBuildFailed() {
                Log.e(TAG, "Could not build route");
            }
        }));

        // Specify fragment transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mSearchFragment.setSharedElementEnterTransition(new TransitionSet().addTransition(new ChangeBounds()).
                    addTransition(new ChangeTransform()).
                    addTransition(new ChangeImageTransform()));

            mSearchFragment.setEnterTransition(new Fade());

            mStartFragment.setExitTransition(new Fade());
        }


        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Shared element to animate in fragment transaction (make sure this view can be retrieved)
        if (mStartFragment.getView() != null) {

            final ConstraintLayout destinationEditContainer = mStartFragment.getView().findViewById(R.id.start_destination_edit_container);

            transaction.addSharedElement(destinationEditContainer, getString(R.string.search_edit_container_transition_name));

        }

        // Actual fragment replacement
        transaction.replace(R.id.main_frame_layout, mSearchFragment)
                .addToBackStack(SearchFragment.class.getSimpleName())
                .commit();

        Log.i(TAG, "Loaded search fragment from start fragment");

    }

    private void loadRouteFragment(Route route) {

        if(mRouteFragment == null){
            mRouteFragment = RouteFragment.newInstance(route);
        }else{
            mRouteFragment.loadRoute(route);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_layout, mRouteFragment)
                .addToBackStack(SearchFragment.class.getSimpleName())
                .commit();
    }

    private void loadSearchFragment(SearchFragment.OnWaypointSelectedCallback listener){

        mSearchFragment = SearchFragment.newInstance(listener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSearchFragment.setEnterTransition(new Fade());
            mRouteFragment.setExitTransition(new Fade());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_frame_layout, mSearchFragment)
                .addToBackStack(SearchFragment.class.getSimpleName())
                .commit();


    }

    @Override
    public void loadSearchFragmentForOrigin() {
        loadSearchFragment((waypoint, placeName) -> buildRoute(waypoint, placeName, destination, destinationPlaceName, startTime, new OnRouteBuiltListener() {
            @Override
            public void onRouteBuilt(Route route) {
                loadRouteFragment(route);
                hideSearchFragment();
            }

            @Override
            public void onRouteBuildFailed() {
                Log.e(TAG,"Could not build route");
            }
        }));
    }

    @Override
    public void loadSearchFragmentForDestination() {
        loadSearchFragment((waypoint, placeName) -> buildRoute(origin, originPlaceName, waypoint, placeName, startTime, new OnRouteBuiltListener() {
            @Override
            public void onRouteBuilt(Route route) {
                loadRouteFragment(route);
                hideSearchFragment();
            }

            @Override
            public void onRouteBuildFailed() {
                Log.e(TAG,"Could not build route");
            }
        }));
    }

    private void hideSearchFragment() {

        getSupportFragmentManager()
                .beginTransaction()
                .remove(mSearchFragment)
                .commit();

    }

    @Override
    public void flipRoute(Route route) {
        buildFlippedRoute(route, new OnRouteBuiltListener() {
            @Override
            public void onRouteBuilt(Route route) {
                loadRouteFragment(route);
            }

            @Override
            public void onRouteBuildFailed() {

            }
        });
    }

    @Override
    public void changeRouteStartTime(long startTime) {

        this.startTime = startTime;

        buildRoute(origin, originPlaceName, destination, destinationPlaceName, startTime, new OnRouteBuiltListener() {
            @Override
            public void onRouteBuilt(Route route) {
                loadRouteFragment(route);
            }

            @Override
            public void onRouteBuildFailed() {
                Log.e(TAG,"Could not build route");
            }
        });
    }

    private interface OnRouteBuiltListener {
        void onRouteBuilt(Route route);
        void onRouteBuildFailed();
    }

    private void buildRoute(final Waypoint origin, final String originPlaceName, final Waypoint destination, final String destinationPlaceName, long startTime, OnRouteBuiltListener callback){

        // Make sure all the provided values are valid
        if(!allRouteValuesAreValid(origin,originPlaceName,destination,destinationPlaceName,startTime)){
            Log.e(TAG,"Not all route values were valid");
            return;
        }

        loadLoadingFragment();

        this.origin = origin;
        this.originPlaceName = originPlaceName;
        this.destination = destination;
        this.destinationPlaceName = destinationPlaceName;
        this.startTime = startTime;

        JSONRetriever.sendRouteRequest(this,origin, destination, json -> {

            // Try to generate a route object from json response
            final RouteBuilder builder = new RouteBuilder(this)
                    .setOrigin(origin,originPlaceName)
                    .setDestination(destination,destinationPlaceName)
                    .setStartTime(startTime)
                    .withJson(json);

            builder.build(route -> {

                if(route != null){

                    callback.onRouteBuilt(route);

                    return;
                }

                callback.onRouteBuildFailed();
            });

        });


    }

    private void buildFlippedRoute(Route toBeFlipped, OnRouteBuiltListener callback){

        buildRoute(toBeFlipped.getDestination(),
                toBeFlipped.getDestinationPlaceName(),
                toBeFlipped.getOrigin(),
                toBeFlipped.getOriginPlaceName(),
                startTime,
                callback);

    }

    private boolean allRouteValuesAreValid(final Waypoint origin, final String originPlaceName, final Waypoint destination, final String destinationPlaceName, long startTime){

        /*Log.i(TAG,
                ", Origin: " + String.valueOf(origin.getLatitude())
                + ", " + String.valueOf(origin.getLongitude())
                + ", OriginPlaceName: " + originPlaceName
                + ", Destination: " + String.valueOf(destination.getLatitude())
                + ", " + String.valueOf(destination.getLongitude())
                + ", DestinationPlaceName: " + destinationPlaceName
                + ", StartTime: " + String.valueOf(startTime));*/

        return origin != null
                && originPlaceName != null
                && destination != null
                && destinationPlaceName != null
                && startTime > 0;

    }


    // Handle back presses
    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {

            // Finish activity if back stack is empty of fragments on back button press
            finish();

            return;
        }

        // Otherwise let super handle the back press
        super.onBackPressed();
    }

}
