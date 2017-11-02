package com.android.locationupdatesisolate;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

public class MyLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Looper mLooper;
    private Boolean isRunning = false;
    private FusedLocation mLocProvider;
    private FusedLocationProviderClient mLocationProviderClient;
    private Boolean requestingLocation;
    private SettingsClient mSettingsClient;
    private Location mCurrentLocation;
    private boolean permissionEnabled;
    private PendingIntent mPendingIntent;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private String userID;
    private Activity activity;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    public MyLocationService() {
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onCreate() {
        super.onCreate();
        createLocationCallback();
        createLocationRequest();
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
                if (!permissionEnabled) {

                }
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("runnable permission", "permission check failed");
                    permissionEnabled = false;
                    sendLocalBroadcast();
                    Thread.currentThread().interrupt();
                    return;
                }
                mLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                mLocationProviderClient.setMockMode(true);
                mLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                if (isRunning) {
                    Log.i("runnable", "is running");
                }
                Looper.loop();
            }
        }, "LocationThread").start();


        return super.onStartCommand(intent, flags, startId);
    }

    public void killService() {
        Log.i("kill", "service killed");
        isRunning = false;
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

    public void sendLocalBroadcast() {
        Intent intent = new Intent();
        intent.putExtra("location_enabled", false);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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
