package com.example.hardeepsingh.onboardcomputer.handlers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Calculate GM Path
 *
 * @author by Hardeep Singh (hardeepsingh@cpp.edu)
 * January 23, 2018
 */
public class GMPath {

    private List<LatLng> wayPoints = new ArrayList<>();

    public interface ResponseInterface {
        void onDataReceived(JSONObject jsonObject);
    }

    public String generateURL(LatLng origin, LatLng destination) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=cycling";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    public void getDirectionJSON(String url, final ResponseInterface callback, Context context) {
        final JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Pass Data Back to Caller
                callback.onDataReceived(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("GMPATH VolleyException:", "Error making Volley Request: " + error.getMessage());
            }
        });
        VolleySingleton.getInstance(context).addToRequestQueue(jsObjRequest);
    }

    public ArrayList<ArrayList<HashMap<String, String>>> parseDirectionJSON(JSONObject jsonObject) {
        ArrayList<ArrayList<HashMap<String, String>>> routes = new ArrayList<>();
        Log.d("Hsing", jsonObject.toString());
        JSONArray jRoutes, jLegs, jSteps;
        try {
            jRoutes = jsonObject.getJSONArray("routes");
            /** Traversing all routes */
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                ArrayList path = new ArrayList<>();

                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline;
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        ArrayList<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude));
                            hm.put("lng", Double.toString((list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
        } catch (JSONException e) {
            Log.e("GMPATH JSONException:", "Failed parse JSON, JSONExection " + e.getMessage());
        } catch (Exception e) {
            Log.e("GMPATH ParseException:", "Failed parse JSON, ParseException " + e.getMessage());
        }
        return routes;
    }

    /**
     * Create PolyLine Options from JsonObject of ArrayList
     *
     * @param result
     * @return
     */
    public PolylineOptions generatePathPolyLine(ArrayList<ArrayList<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route and add to polyline
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            lineOptions.addAll(points);
            lineOptions.width(20);
            lineOptions.color(Color.BLUE);

            //Store way points
            wayPoints.addAll(points);
        }

        // Drawing polyline
        if (lineOptions != null) {
            return lineOptions;
        } else {
            Log.d("Maps:", "PolyLine MarkerOptions are empty!");
        }
        return null;
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

    /**
     * Method to decode polyline points
     * Courtesy : Jeffery Sambells
     * http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private ArrayList<LatLng> decodePoly(String encoded) {
        // Google Map provided encoder
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }
            while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}