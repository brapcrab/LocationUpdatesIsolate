package com.android.locationupdatesisolate;

import android.*;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.LocationListener;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_LOCATION_REQUEST_CODE = 1;
    private boolean permissionEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intent intent = new Intent(this, LocationService.class);
        //intent.setAction("com.android.locationupdatesisolate.action.START_UPDATES");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                mMessageReceiver, new IntentFilter("location_enabled"));
        Intent intent = new Intent(this, MyLocationService.class);
        startService(intent);
        //LocationService.startUpdates(this, "test", "best");
        if (isMyServiceRunning(MyLocationService.class)){
            Log.i("startService", "Service has started");
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mMessageReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("test", "requested permissions");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }
    }
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            permissionEnabled = intent.getExtras().getBoolean("location_enabled");
            Log.i("receiver", "broadcast received");
            if (!permissionEnabled) {
                requestPermission();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("permission result", "permission granted");
                    if (!isMyServiceRunning(MyLocationService.class)) {
                        startService(new Intent().setAction("com.android.locationupdatesisolate.MyLocationService"));
                    }
                }
                break;
        }
    }



    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}