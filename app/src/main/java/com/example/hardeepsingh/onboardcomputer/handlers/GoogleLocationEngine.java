package com.example.hardeepsingh.onboardcomputer.handlers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Google Location Provider Engine
 *
 * @author hardeepsingh on March 28,2018
 */
public class GoogleLocationEngine {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Context context;

    public GoogleLocationEngine(Context context) {
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdate(final LocationUpdateListener locationUpdateListener) {
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(300)
                .setFastestInterval(300);


        fusedLocationProviderClient.requestLocationUpdates(request, new LocationDelegate(locationUpdateListener), null);
    }

    public void stopLocationUpdates(LocationUpdateListener locationUpdateListener) {
        fusedLocationProviderClient.removeLocationUpdates(new LocationDelegate(locationUpdateListener));
    }


    public interface LocationUpdateListener {
        void onLocationUpdate(Location location);
    }

    public class LocationDelegate extends LocationCallback {

        private LocationUpdateListener locationUpdateListener;

        public LocationDelegate(LocationUpdateListener locationUpdateListener) {
            this.locationUpdateListener = locationUpdateListener;
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            locationUpdateListener.onLocationUpdate(locationResult.getLastLocation());
        }
    }
}
