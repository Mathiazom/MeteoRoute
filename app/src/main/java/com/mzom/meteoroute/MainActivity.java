package com.mzom.meteoroute;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.mapboxsdk.Mapbox;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements StartFragment.LoadSearchFromStartCallback, RouteFragment.RouteFragmentListener, LoadingManager {

    private static final String TAG = "MRU-MainActivity";


    private StartFragment mStartFragment;

    private SearchFragment mSearchFragment;

    private RouteFragment mRouteFragment;

    private LoadingFragment mLoadingFragment;

    private long startTime;

    private Waypoint origin;
    private String originPlaceName;

    private Waypoint destination;
    private String destinationPlaceName;

    private FusedLocationProviderClient mFusedLocationClient;

    /*private Location mCurrentLocation;


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
    };*/

    private final OnSuccessListener<Location> mFusedLocationListener = new OnSuccessListener<Location>() {
        @Override
        public void onSuccess(Location location) {
            if (location != null) {

                //mCurrentLocation = location;

                startTime = Calendar.getInstance().getTimeInMillis();
                origin = new Waypoint(location.getLongitude(), location.getLatitude());
                originPlaceName = "Your location";
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.mapbox_api_key));

        setContentView(R.layout.activity_main);

        if (!hasCurrentLocationPermissions()) {
            loadPermissionsFragment();
        }

        Log.i(TAG, "All permissions are granted");

        // Try to retrieve current location if necessary permissions are granted
        requestLocationUpdates();

        // Let user search for and pick route destination while app is retrieving current location (route origin)
        loadStartFragment();

    }

    // Check if application has the necessary permissions to get device's current location
    private boolean hasCurrentLocationPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Request user permissions with PermissionsFragment
    private void loadPermissionsFragment() {

        final PermissionsFragment permissionsFragment = PermissionsFragment.newInstance(() -> {

            // Continue to StartFragment without location permissions
            loadStartFragment();

        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_layout, permissionsFragment)
                .addToBackStack(StartFragment.class.getSimpleName())
                .commit();

    }


    @SuppressLint("MissingPermission")
    // Permissions check is done with hasCurrentLocationPermissions()
    private void requestLocationUpdates() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!hasCurrentLocationPermissions()) {
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, mFusedLocationListener);

        /*final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null) {

            if (!hasCurrentLocationPermissions()) {
                return;
            }

            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, mFusedLocationListener);

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);
        }*/
    }

    private static final int PERMISSION_REQUEST_CODE = 1600;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {

            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_DENIED) {

                    Log.e(TAG, permissions[i] + " is not granted");

                    return;
                }
            }

            Log.i(TAG, "All permissions are granted");

            // Try to retrieve current location if necessary permissions are granted
            requestLocationUpdates();

            // Let user search for and pick route destination while app is retrieving current location (route origin)
            loadStartFragment();

        }

    }

    private void loadLoadingFragment(String loadingMessage) {

        mLoadingFragment = LoadingFragment.newInstance(loadingMessage);

        // Specify fragment transitions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            mLoadingFragment.setSharedElementEnterTransition(new TransitionSet().addTransition(new ChangeBounds()).
                    addTransition(new ChangeTransform()).
                    addTransition(new ChangeImageTransform()));

            mLoadingFragment.setEnterTransition(new Fade());
        }

        // Actual fragment replacement
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, mLoadingFragment)
                .addToBackStack(LoadingFragment.class.getSimpleName())
                .commit();

    }

    private void loadStartFragment() {

        mStartFragment = new StartFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_layout, mStartFragment)
                .addToBackStack(StartFragment.class.getSimpleName())
                .commit();
    }

    // Transaction-specific method between start and search necessary to display shared element animation (route destination EditText)
    @Override
    public void loadSearchFragmentFromStart() {

        // Create fragment to search for route destination
        mSearchFragment = SearchFragment.newInstance((waypoint, placeName) -> {

            if (origin == null) {

                if (hasCurrentLocationPermissions()) {
                    // Current location has not been retrieved asynchronously
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                        mFusedLocationListener.onSuccess(location);
                        buildRoute(origin, originPlaceName, waypoint, placeName, startTime, new OnRouteBuiltListener() {
                            @Override
                            public void onRouteBuilt(Route route) {
                                loadRouteFragment(route);
                                hideSearchFragment();
                            }

                            @Override
                            public void onRouteBuildingFailed() {
                                Log.e(TAG, "Could not build route");
                            }
                        });
                    });
                    return;
                }

                Log.e(TAG, "Origin was null");
                return;
            }

            buildRoute(origin, originPlaceName, waypoint, placeName, startTime, new OnRouteBuiltListener() {
                @Override
                public void onRouteBuilt(Route route) {
                    loadRouteFragment(route);
                    hideSearchFragment();
                }

                @Override
                public void onRouteBuildingFailed() {
                    Log.e(TAG, "Could not build route");
                }
            });
        });

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

            // Shared element
            final ConstraintLayout destinationEditContainer = mStartFragment.getView().findViewById(R.id.start_destination_edit_container);

            // Include shared element in fragment transaction
            transaction.addSharedElement(destinationEditContainer, getString(R.string.search_edit_container_transition_name));

        }

        // Actual fragment replacement
        transaction.replace(R.id.main_frame_layout, mSearchFragment)
                .addToBackStack(SearchFragment.class.getSimpleName())
                .commit();

    }

    // Display route with RouteFragment
    private void loadRouteFragment(Route route) {

        setLoadingMessage("Loading route");

        if (mRouteFragment == null) {
            // Create new route fragment with new route
            mRouteFragment = RouteFragment.newInstance(route);
        } else {
            // Load new route to already existing fragment
            mRouteFragment.loadRoute(route);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_layout, mRouteFragment)
                //.addToBackStack(SearchFragment.class.getSimpleName())
                .commit();
    }

    // Let user search for and pick location (as origin or destination)
    private void loadSearchFragment(SearchFragment.OnWaypointSelectedCallback listener) {

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

    // Let user search for and pick desired route origin
    @Override
    public void loadSearchFragmentForOrigin() {
        loadSearchFragment((waypoint, placeName) -> buildRoute(waypoint, placeName, destination, destinationPlaceName, startTime, new OnRouteBuiltListener() {
            @Override
            public void onRouteBuilt(Route route) {
                loadRouteFragment(route);
                hideSearchFragment();
            }

            @Override
            public void onRouteBuildingFailed() {
                Log.e(TAG, "Could not build route");
            }
        }));
    }

    // Let user search for and pick desired route destination
    @Override
    public void loadSearchFragmentForDestination() {
        loadSearchFragment((waypoint, placeName) -> buildRoute(origin, originPlaceName, waypoint, placeName, startTime, new OnRouteBuiltListener() {
            @Override
            public void onRouteBuilt(Route route) {
                loadRouteFragment(route);
                hideSearchFragment();
            }

            @Override
            public void onRouteBuildingFailed() {
                Log.e(TAG, "Could not build route");
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
            public void onRouteBuildingFailed() {

            }
        });
    }

    // Build new route where only start time is changed
    @Override
    public void changeRouteStartTime(long startTime) {

        if (startTime < 0) {
            Log.e(TAG, "Invalid route start time: " + String.valueOf(startTime));
            return;
        }

        this.startTime = startTime;

        buildRoute(origin, originPlaceName, destination, destinationPlaceName, startTime, new OnRouteBuiltListener() {
            @Override
            public void onRouteBuilt(Route route) {
                loadRouteFragment(route);
            }

            @Override
            public void onRouteBuildingFailed() {
                Log.e(TAG, "Could not build route");
            }
        });
    }

    @Override
    public void setLoadingMessage(String message) {

        if(mLoadingFragment == null){
            loadLoadingFragment(message);
            return;
        }

        mLoadingFragment.setLoadingMessage(message);

    }

    private interface OnRouteBuiltListener {
        void onRouteBuilt(Route route);

        void onRouteBuildingFailed();
    }

    private void buildRoute(final Waypoint origin, final String originPlaceName, final Waypoint destination, final String destinationPlaceName, long startTime, OnRouteBuiltListener callback) {

        // Make sure all the provided values are valid
        if (!RouteValuesValidator.validateInputs(origin, originPlaceName, destination, destinationPlaceName, startTime)) {
            Log.e(TAG, "Not all route values were valid");
            return;
        }

        // Display a loading screen while route is being built
        loadLoadingFragment("Building route");

        this.origin = origin;
        this.originPlaceName = originPlaceName;
        this.destination = destination;
        this.destinationPlaceName = destinationPlaceName;
        this.startTime = startTime;

        // Start route creation
        final RouteBuilder builder = new RouteBuilder(this)
                .setOrigin(origin, originPlaceName)
                .setDestination(destination, destinationPlaceName)
                .setStartTime(startTime);

        builder.build(new RouteBuilder.RouteBuilderListener() {
            @Override
            public void onRouteBuilt(@NonNull Route route) {
                callback.onRouteBuilt(route);
            }

            @Override
            public void onRouteBuildingFailed() {
                callback.onRouteBuildingFailed();
            }
        });


    }

    private void buildFlippedRoute(Route toBeFlipped, OnRouteBuiltListener callback) {

        buildRoute(toBeFlipped.getDestination(),
                toBeFlipped.getDestinationPlaceName(),
                toBeFlipped.getOrigin(),
                toBeFlipped.getOriginPlaceName(),
                startTime,
                callback);

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
