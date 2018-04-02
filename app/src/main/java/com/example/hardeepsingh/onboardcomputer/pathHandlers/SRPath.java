package com.example.hardeepsingh.onboardcomputer.pathHandlers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.hardeepsingh.onboardcomputer.utils.OnBoardUtil;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Get Path Way-Points from Server
 *
 * @author hardeepsingh on March 30,2018
 */
public class SRPath {
    //TODO Replace with actual serve url
    private static final String BASE_URL = "http://ip.jsontest.com/";
    private List<LatLng> wayPoints = new ArrayList<>();

    public void getDirectionJSON(Context context, LatLng origin, LatLng destination, TransitType transitType, final ResponseInterface callback) {
        //TODO: Add Params as required
        String url = BASE_URL;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onDataReceived(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("SRPATH VolleyException:", "Error making Volley Request: " + error.getMessage());
            }
        });
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * File name and Context is passed into this method because there is no server yet
     * TODO: Once server is up and running, only passs jsonObject and parse is accordingly for way-points and polyline
     * @param jsonObject
     * @param fileName
     * @param context
     * @return
     */
    public PolylineOptions generatePathPolyLine(Context context, JSONObject jsonObject, String fileName) {
        PolylineOptions polylineOptions = new PolylineOptions();
        wayPoints.clear();
        wayPoints.addAll(OnBoardUtil.readCordinatesFromFile(context, fileName));

        polylineOptions.addAll(wayPoints);
        polylineOptions.width(20);
        polylineOptions.color(Color.BLUE);
        return polylineOptions;
    }

    /**
     * Create PolyLineOptions from File
     *
     * @return
     */

    public List<LatLng> getWayPoints() {
        if (!wayPoints.isEmpty()) {
            return wayPoints;
        }
        return null;
    }
}
