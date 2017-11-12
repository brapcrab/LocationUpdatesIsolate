package com.android.locationupdatesisolate;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.BlockingQueue;

public class MomentsListActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private GeoFire geoFire;
    private Location mCurrentLocation;
    private LocationCallback mLocationCallback;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton logOut;
    @BindView(R.id.location) FloatingActionButton location;
    private FirebaseUser mCurrentUser;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        createGeoFire();
    }


    private void createGeoFire() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("Locations");
        geoFire = new GeoFire(mDatabaseReference);
    }

    @OnClick(R.id.fab)
    public void signOut(View view) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                });
            }

    @OnClick(R.id.location)
    public void locationGetter() {
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser != null) {
            if (mCurrentLocation != null) {
                if ((mCurrentLocation.getTime() + 100) < System.currentTimeMillis()) {
                    String locString = "Latitude = " + mCurrentLocation.getLatitude() + ", Longitude = " + mCurrentLocation.getLongitude();
                    Toast.makeText(getApplicationContext(),
                            locString, Toast.LENGTH_LONG).show();
                } else mCurrentLocation = LocationProviderService.getCurrentLocation();
            }
            else {
                userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Log.i("userid", userID);
                geoFire.getLocation(userID, new com.firebase.geofire.LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        if (location != null) {
                            String locString = "Latitude = " + location.latitude + ", Longitude = " + location.longitude;
                            Toast.makeText(getApplicationContext(),
                                    locString, Toast.LENGTH_LONG).show();
                        } else Log.i("location getter", "location is null");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("location getter", databaseError.toString());
                    }
                });
            }
        }
    }

}