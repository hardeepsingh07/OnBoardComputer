package com.example.hardeepsingh.onboardcomputer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.example.hardeepsingh.onboardcomputer.R;
import com.example.hardeepsingh.onboardcomputer.databinding.ActivityWaypointRouteBinding;
import com.example.hardeepsingh.onboardcomputer.handlers.GMPath;
import com.example.hardeepsingh.onboardcomputer.handlers.GoogleLocationEngine;
import com.example.hardeepsingh.onboardcomputer.models.Building;
import com.example.hardeepsingh.onboardcomputer.utils.AnimationUtil;
import com.example.hardeepsingh.onboardcomputer.utils.OnBoardUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaypointRoute extends FragmentActivity
        implements OnMapReadyCallback, GoogleLocationEngine.LocationUpdateListener {

    //Average Speed of Bicycle is 15.5km/h
    private static final int SPEED_PER_METER_PER_MINUTE = 258;

    private GoogleMap map;
    private Building building;
    protected Location lastLocation, startLocation, destinationLocation;
    private Marker currentMarker;
    private ActivityWaypointRouteBinding binding;
    private GoogleLocationEngine googleLocationEngine;
    private boolean inProgress;

    private ArrayList<LatLng> wayPoints = new ArrayList<>();
    private boolean simulate;
    private String wayPointFileName;
    private Runnable runnable;
    private static Handler handler;

    public static Intent createIntent(Context context, Building building, String fileName, boolean simulate) {
        Intent intent = new Intent(context, WaypointRoute.class);
        intent.putExtra("building", building);
        intent.putExtra("fileName", fileName);
        intent.putExtra("simulate", simulate);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        building = (Building) intent.getSerializableExtra("building");
        wayPointFileName = intent.getStringExtra("fileName");
        simulate = intent.getBooleanExtra("simulate", false);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_waypoint_route);
        binding.setBuilding(building);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initialize Google Location Engine
        googleLocationEngine = new GoogleLocationEngine(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleLocationEngine != null) {
            googleLocationEngine.startLocationUpdate(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleLocationEngine != null) {
            googleLocationEngine.stopLocationUpdates(this);
        }
    }

    /**
     * Screen Actions
     *
     * @param view
     */
    public void startNavigation(View view) {
        if (startLocation == null) {
            Toast.makeText(this, "Cannot find user location. Please Turn on GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        if (simulate) {
            simulateMarker();
        }
        inProgress = !simulate;
        animateToMarker(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        AnimationUtil.showInRoutePanel(binding);
    }

    public void stopNavigation(View View) {
        inProgress = false;
        zoomRoute();
        AnimationUtil.showLaunchPanel(binding);

        //Stop Handler and Remove Calls
        if (handler != null && simulate) {
            handler.removeCallbacks(runnable);
        }
    }

    public void travelBack(View view) {
        Collections.reverse(wayPoints);
        AnimationUtil.showLaunchPanel(binding);
    }

    public void newSelection(View view) {
        finish();
    }

    @SuppressLint("SetTextI18n")
    public void updateBindings() {
        double distanceInMeters = calculateDistance(lastLocation, destinationLocation);
        String distance = OnBoardUtil.convertMeterToMilesString(distanceInMeters);
        binding.distance.setText(distance);


        double estimatedDriveTimeInMinutes = distanceInMeters / SPEED_PER_METER_PER_MINUTE;
        String duration = OnBoardUtil.convertMinutesToHourMinutes(estimatedDriveTimeInMinutes);
        binding.duration.setText(duration);
    }

    @SuppressLint("SetTextI18n")
    public void updateProgressBindings() {
        double distanceInMeters = calculateDistance(lastLocation, destinationLocation);
        String distance = OnBoardUtil.convertMeterToMilesString(distanceInMeters);
        binding.distanceRemainingText.setText(distance);


        double estimatedDriveTimeInMinutes = distanceInMeters / SPEED_PER_METER_PER_MINUTE;
        String duration = OnBoardUtil.convertMinutesToHourMinutes(estimatedDriveTimeInMinutes);
        binding.timeRemainingText.setText(duration);
    }

    /**
     * Map and Location Updates
     *
     * @param googleMap
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationUpdate(Location location) {
        //Store user last location for Marker
        lastLocation = location;

        //Setup Map Extracting User Location (Origin) and Building Location (Destination)
        if (startLocation == null) {
            setUpInitialTransit(location);
        }

        //If InProgress and not simulating, Track user location and update marker
        if (inProgress && !simulate) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            updateMarker(latLng);
            destinationReached(latLng);

            //Update Duration and Distance
            updateProgressBindings();
        }
    }

    public void setUpInitialTransit(Location currentLocation) {
        if (building != null) {
            LatLng source = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            LatLng destination = new LatLng(building.getLatitude(), building.getLongitude());

            //Store Destination Location for Distance Calculation
            this.destinationLocation = OnBoardUtil.convertCoOrdinationToLocation(building.getLatitude(), building.getLongitude());

            //Store Start location for Travel Back
            this.startLocation = currentLocation;

            //If wayPointFile name doesn't exist, calculate the path else extract wayPoints from file
            if (TextUtils.isEmpty(wayPointFileName)) {
                generateDirections(source, destination);
            } else {
                createPolyOptions(OnBoardUtil.readCordinatesFromFile(this, wayPointFileName), null);
            }
        } else {
            Toast.makeText(this, "No Building Data Found!", Toast.LENGTH_SHORT).show();
        }
    }

    public void generateDirections(LatLng source, LatLng destination) {
        final GMPath gmPath = new GMPath();
        String url = gmPath.generateURL(source, destination);
        Log.i("Maps: ", "Direction URL: " + url);

        // Make URL Call and Receive Data using ResponseInterface
        gmPath.getDirectionJSON(url, new GMPath.ResponseInterface() {
            @Override
            public void onDataReceived(JSONObject jsonObject) {
                PolylineOptions polylineOptions = gmPath.generatePathPolyLine(gmPath.parseDirectionJSON(jsonObject));
                createPolyOptions(gmPath.getWayPoints(), polylineOptions);
            }
        }, this);
    }

    /**
     * Design the Map
     */
    public void createPolyOptions(List<LatLng> points, PolylineOptions polylineOptions) {
        PolylineOptions options;
        wayPoints.clear();
        if (!TextUtils.isEmpty(wayPointFileName)) {
            wayPoints.addAll(points);
            options = new PolylineOptions();
            options.addAll(wayPoints);
            options.width(20);
            options.color(Color.BLUE);
        } else {
            wayPoints.addAll(points);
            options = polylineOptions;
        }

        makePolyLine(options);
    }

    public void makePolyLine(PolylineOptions polylineOptions) {
        //Draw a Polyline
        if (wayPoints != null && polylineOptions != null) {
            map.addPolyline(polylineOptions);

            //Zoom On Route
            zoomRoute();

            //Update Bindings
            updateBindings();

            //Show Launch Panel
            AnimationUtil.showLaunchPanel(binding);

            //Add Marker To Start and End
            markStartAndEnd();

            //Remove Progress Bar
            binding.progressBar.setVisibility(View.GONE);

            //Place user marker on the route
            createMarker(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        }
    }

    public void markStartAndEnd() {
        //Add Marker To Start and END
        map.addMarker(new MarkerOptions().position(wayPoints.get(0))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title("Start"));
        map.addMarker(new MarkerOptions().position(wayPoints.get(wayPoints.size() - 1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title("End"));
    }

    public double calculateDistance(Location startLocation, Location endLocation) {
        return startLocation.distanceTo(endLocation);
    }

    public void destinationReached(LatLng current) {
        if (wayPoints.size() > 0) {
            LatLng destination = wayPoints.get(wayPoints.size() - 1);
            if (SphericalUtil.computeDistanceBetween(current, destination) < 1) {
                AnimationUtil.showDestinationPanel(binding);
            }
        }
    }

    public void createMarker(LatLng latLng) {
        if (currentMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("User Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            currentMarker = map.addMarker(markerOptions);
        }
    }

    public void updateMarker(LatLng latLng) {
        createMarker(latLng);
        currentMarker.setPosition(latLng);
        animateToMarker(latLng);
    }

    public void animateToMarker(LatLng latLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
    }

    public void zoomRoute() {
        if (map == null || wayPoints == null || wayPoints.isEmpty()) return;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : wayPoints)
            boundsBuilder.include(latLngPoint);
        int routePadding = 700;
        LatLngBounds latLngBounds = boundsBuilder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
    }

    private void simulateMarker() {
        handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 30000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();

        runnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                if (i < wayPoints.size()) {
                    LatLng current = wayPoints.get(i);
                    currentMarker.setPosition(current);
                    animateToMarker(currentMarker.getPosition());
                    lastLocation = OnBoardUtil.convertLatLngToLocation(current);
                    updateProgressBindings();
                    destinationReached(current);
                }
                i++;
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 1500);
                }
            }
        };
        handler.post(runnable);
    }
}
