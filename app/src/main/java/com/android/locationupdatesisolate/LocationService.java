package com.android.locationupdatesisolate;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.icu.text.DateFormat;
import android.location.Location;
import android.os.Looper;
import android.support.design.widget.BaseTransientBottomBar;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocationService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPDATES = "com.android.locationupdatesisolate.action.START_UPDATES";
    private static final String ACTION_REQUEST = "com.android.locationupdatesisolate.action.REQUEST_LOC";
    private FusedLocation mLocProvider;
    private FusedLocationProviderClient mLocationProviderClient;
    private Boolean requestingLocation;
    private SettingsClient mSettingsClient;
    private Location mCurrentLocation;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private String userID;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.android.locationupdatesisolate.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.android.locationupdatesisolate.extra.PARAM2";

    public LocationService() {
        super("LocationService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startUpdates(Context context, String param1, String param2) {
        Log.i("startUpdates","startUpdates Started");
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(ACTION_UPDATES);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRequest(Context context, String param1, String param2) {
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(ACTION_REQUEST);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("onHandleIntent", "Called onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATES.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionUpdates(param1, param2);
            } else if (ACTION_REQUEST.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionRequest(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    @SuppressWarnings({"MissingPermission"})
    private void handleActionUpdates(String param1, String param2) {
        // TODO: Start updates
        Log.i("handleActionUpdates", "foobarred");
        createLocationRequest();
        createLocationCallback();
        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper());
        /*
        mLocProvider = new FusedLocation(this, new FusedLocation.Callback(){
            @Override
            public void onLocationResult(Location location){
                String result = "lat " + location.getLatitude() + " long " + location.getLongitude();
                Log.i("loc_result", "onLocationResult called");
                //Do as you wish with location here
                //userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                //GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
                //geoFire.setLocation(userID, geoLocation);
                //System.out.println(geoLocation.latitude + geoLocation.longitude);
            }});
        if (!mLocProvider.canGetLocation()) {
            mLocProvider.showSettingsAlert();
        }
        else {
            mLocProvider.startLocationUpdates();
        }
        mLocProvider.getCurrentLocation(1);
        */
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRequest(String param1, String param2) {
        // TODO: Request location
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i("loc result", "called");
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                if (mCurrentLocation != null) {
                    Log.i("location result", "location received");
                }
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }



}
