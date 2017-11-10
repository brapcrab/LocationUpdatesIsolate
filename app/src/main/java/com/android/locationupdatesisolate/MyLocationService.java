package com.android.locationupdatesisolate;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Looper mLooper;
    private Boolean isRunning = false;
    private FusedLocationProviderClient mLocationProviderClient;
    private Boolean requestingLocation;
    private SettingsClient mSettingsClient;
    private Location mCurrentLocation;
    private static Location currentLocation;
    private boolean permissionEnabled;
    private PendingIntent mPendingIntent;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private String userID;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private GeoFire geoFire;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    public MyLocationService() {
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onCreate() {
        super.onCreate();
        //AppOpsManager.checkOp("android:mock_location", "yourUID", "com.android.locationupdatesisolate");
        createLocationCallback();
        createLocationRequest();
        createGeoFire();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        //HandlerThread thread = new HandlerThread("LocationThread", Process.THREAD_PRIORITY_BACKGROUND);
        new Thread(new Runnable() {
            @Override
            @SuppressWarnings({"MissingPermission"})
            public void run() {
                Looper.prepare();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("runnable permission", "permission check failed");
                    permissionEnabled = false;
                    sendLocalBroadcast();
                    //Thread.currentThread().interrupt();
                    return;
                }
                mLocationProviderClient = LocationServices.getFusedLocationProviderClient(MyLocationService.this);
                //mLocationProviderClient.setMockMode(true);
                mLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                if (isRunning) {
                    Log.i("runnable", "is running");
                }
                Looper.loop();
            }
        }, "LocationThread").start();


        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationProviderClient.removeLocationUpdates(mLocationCallback);
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
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

    private void createGeoFire() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("Locations");
        geoFire = new GeoFire(mDatabaseReference);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.i("loc result", "called");
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                if (mCurrentLocation != null) {
                    geoFire.setLocation("PutUidHere",new GeoLocation(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));
                    Log.i("location result", "location received");
                }
            }
        };
    }

    public void sendLocalBroadcast() {
        Intent intent = new Intent("location_enabled");
        intent.putExtra("location_services", false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.i("broadcastsender", "broadcast sent");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
