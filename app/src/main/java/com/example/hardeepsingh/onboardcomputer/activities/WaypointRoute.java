package com.example.hardeepsingh.onboardcomputer.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.hardeepsingh.onboardcomputer.R;
import com.example.hardeepsingh.onboardcomputer.databinding.ActivityWaypointRouteBinding;
import com.example.hardeepsingh.onboardcomputer.pathHandlers.GMPath;
import com.example.hardeepsingh.onboardcomputer.pathHandlers.GoogleLocationEngine;
import com.example.hardeepsingh.onboardcomputer.models.Building;
import com.example.hardeepsingh.onboardcomputer.pathHandlers.ResponseInterface;
import com.example.hardeepsingh.onboardcomputer.pathHandlers.SRPath;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Way-points Route class handle navigation
 *
 * @author by Hardeep Singh (hardeepsingh@cpp.edu)
 * March 23, 2018
 */
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

    private PolylineOptions transitPolyOptions;
    private Polyline transitPolyLine;
    private List<LatLng> userMovementPoints = new ArrayList<>();

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
    protected void onResume() {
        super.onResume();
        if (googleLocationEngine != null) {
            googleLocationEngine.startLocationUpdate(this);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleLocationEngine != null) {
            googleLocationEngine.stopLocationUpdates(this);
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Screen Actions Buttons for User Interactions
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
            removeTransitPolyLine();
        }
    }

    public void travelBack(View view) {
        Collections.reverse(wayPoints);
        AnimationUtil.showLaunchPanel(binding);
        removeTransitPolyLine();
    }

    public void newSelection(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Not valid, please use actions buttons!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback for onMapReady, notifies if google maps is ready to be used
     *
     * @param googleMap
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
    }


    /**
     * Location Updates from FusedLocationProvider
     * Initially only one location update is ccaptured till user  start the transit
     *
     * @param location
     */
    @Override
    public void onLocationUpdate(Location location) {
        //Store user last location for Marker
        lastLocation = location;

        //Setup Map Extracting User Location (Origin) and Building Location (Destination)
        if (startLocation == null) {
            setUpWithInitialLocation(location);
        }

        //If InProgress and not simulating, Track user location and update marker
        if (inProgress && !simulate) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            updateMarker(latLng);
            destinationReached(latLng);
            createUpdateTransitPolyLine(latLng);

            //Update Duration and Distance
            updateProgressBindings();
        }
    }

    public void createUpdateTransitPolyLine(LatLng latLng) {
        if (transitPolyLine == null && transitPolyOptions == null) {
            transitPolyOptions = new PolylineOptions();
            transitPolyOptions.width(15);
            transitPolyOptions.color(Color.GREEN);
            transitPolyLine = map.addPolyline(transitPolyOptions);
            userMovementPoints.add(latLng);
            transitPolyLine.setPoints(userMovementPoints);
        } else {
            userMovementPoints.add(latLng);
            transitPolyLine.setPoints(userMovementPoints);
        }
    }

    public void removeTransitPolyLine() {
        userMovementPoints.clear();
        transitPolyLine.remove();

        //To trigger recreate, reset TransitPolyLine & TransitPolyOptions
        transitPolyLine = null;
        transitPolyOptions = null;
    }

    /**
     * Setup Initial user location, this method only use very first user location update
     * Actual location updates are no triggered till user start the transit
     * @param currentLocation
     */
    public void setUpWithInitialLocation(Location currentLocation) {
        if (building != null) {
            LatLng source = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            LatLng destination = new LatLng(building.getLatitude(), building.getLongitude());

            //Store Destination Location for Distance Calculation
            this.destinationLocation = OnBoardUtil.convertCoOrdinationToLocation(building.getLatitude(), building.getLongitude());

            //Store Start location for Travel Back
            this.startLocation = currentLocation;

            //If wayPointFile name doesn't exist, calculate the path else extract wayPoints from file
            if (TextUtils.isEmpty(wayPointFileName)) {
                generateDirectionsGM(source, destination);
            } else {
                generateDirectionSR(source, destination);
            }
        } else {
            Toast.makeText(this, "No Building Data Found!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get Path from Server
     * @param origin
     * @param destination
     */
    public void generateDirectionSR(LatLng origin, LatLng destination) {
        final SRPath srPath = new SRPath();
        srPath.getDirectionJSON(this, origin, destination, new ResponseInterface() {
            @Override
            public void onDataReceived(JSONObject jsonObject) {
                PolylineOptions polylineOptions = srPath.generatePathPolyLine(getBaseContext(), jsonObject, wayPointFileName);
                createPathPolyOptions(srPath.getWayPoints(), polylineOptions);
            }
        });
    }

    /**
     * Get Path From Google API
     * @param origin
     * @param destination
     */
    public void generateDirectionsGM(LatLng origin, LatLng destination) {
        final GMPath gmPath = new GMPath();
        gmPath.getDirectionJSON(this, origin, destination, new ResponseInterface() {
            @Override
            public void onDataReceived(JSONObject jsonObject) {
                PolylineOptions polylineOptions = gmPath.generatePathPolyLine(gmPath.parseDirectionJSON(jsonObject));
                createPathPolyOptions(gmPath.getWayPoints(), polylineOptions);
            }
        });
    }

    /**
     * Create Path poly Options
     * This method use way-points to make poly options
     *      Way-points can be generated by provided file or by google maps api
     */
    public void createPathPolyOptions(List<LatLng> points, PolylineOptions polylineOptions) {
        wayPoints.clear();
        wayPoints.addAll(points);
        makePolyLine(polylineOptions);
    }

    /**
     * Draw Path Poly Lines
     * Zoom on route to fit the route on entire screen
     * Update bindings with building data
     * Show launch panel
     * Create Current Location Marker
     * @param polylineOptions
     */
    public void makePolyLine(PolylineOptions polylineOptions) {
        //Draw a Polyline
        if (wayPoints != null && polylineOptions != null) {
            map.addPolyline(polylineOptions);
            zoomRoute();
            updateBindings();
            AnimationUtil.showLaunchPanel(binding);
            markStartAndEnd();
            binding.progressBar.setVisibility(View.GONE);
            createMarker(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        }
    }

    public void markStartAndEnd() {
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

    /**
     * Check if destination is reached by checking if current and destination distance is less than 5 meter away
     * @param current
     */
    public void destinationReached(LatLng current) {
        if (wayPoints.size() > 0) {
            LatLng destination = wayPoints.get(wayPoints.size() - 1);
            if (SphericalUtil.computeDistanceBetween(current, destination) < 5) {
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
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
    }

    /**
     * Zoom on Route to fit the route perfectly in single view to give user an idea of transit
     * Route Padding is used to leave space on left and right of path
     */
    public void zoomRoute() {
        if (map == null || wayPoints == null || wayPoints.isEmpty()) return;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : wayPoints)
            boundsBuilder.include(latLngPoint);
        int routePadding = 300;
        LatLngBounds latLngBounds = boundsBuilder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));
    }


    /**
     * Simulate the marker on route for demo purposes (Requires Simulate Flag)
     * Animation Delay is used to cause a delay between current way-point to next way-point
     */
    private void simulateMarker() {
        handler = new Handler();
        final long animationDelay = 1250;
        runnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                if (i < wayPoints.size()) {
                    LatLng current = wayPoints.get(i);
                    updateMarker(current);
                    createUpdateTransitPolyLine(current);
                    lastLocation = OnBoardUtil.convertLatLngToLocation(current);
                    updateProgressBindings();
                    destinationReached(current);

                    handler.postDelayed(this, animationDelay);
                }
                i++;
            }
        };
        handler.post(runnable);
    }
}
