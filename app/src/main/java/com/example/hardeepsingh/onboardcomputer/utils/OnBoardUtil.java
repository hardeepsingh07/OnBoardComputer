package com.example.hardeepsingh.onboardcomputer.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.hardeepsingh.onboardcomputer.models.Building;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Animation Util Class to Animate Launch, Destination and Route Panels
 *
 * @author Hardeep Singh (hardeepsingh@cpp.edu)
 * December 28,2018
 */
public class OnBoardUtil {
    private static final String BUILDING = "building";
    private static final String ID = "id";
    private static final String NAME = "NAME";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "long";
    private static final String DESCRIPTION = "description";

    /**
     * Parse Building Info XML
     *
     * @param xml
     * @return
     */
    public static ArrayList<Building> parseXML(InputStream xml) {
        ArrayList<Building> buildings = new ArrayList<>();
        XmlPullParserFactory factory;
        XmlPullParser parser;

        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();
            parser.setInput(xml, null);

            int eventType = parser.getEventType();
            Building building = null;
            String text = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();
                        if (tagName.equalsIgnoreCase(BUILDING)) {
                            building = new Building();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagName.equalsIgnoreCase(BUILDING)) {
                            buildings.add(building);
                        } else if (tagName.equalsIgnoreCase(ID)) {
                            building.setId(text);
                        } else if (tagName.equalsIgnoreCase(NAME)) {
                            building.setFullName(text);
                            building.parseName();
                        } else if (tagName.equalsIgnoreCase(LATITUDE)) {
                            building.setLatitude(Double.parseDouble(text));
                        } else if (tagName.equalsIgnoreCase(LONGITUDE)) {
                            building.setLongitude(Double.parseDouble(text));
                        } else if (tagName.equalsIgnoreCase(DESCRIPTION)) {
                            building.setSurroundings(text);
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            Log.e("XMLParser PullException", e.getMessage());
        } catch (IOException e) {
            Log.e("XMLParser IOException ", e.getMessage());
        }
        return buildings;
    }

    /**
     * Convert Meters to Miles
     *
     * @param meters
     * @return Double
     */
    public static double convertMetersToMiles(double meters) {
        return (meters / 1609.344);
    }

    /**
     * Convert Meters to Miles
     *
     * @param meters
     * @return String
     */
    public static String convertMeterToMilesString(double meters) {
        DecimalFormat df = new DecimalFormat("####0.00");
        return df.format(meters / 1609.344) + " miles";
    }

    /**
     * Convert Seconds to Minutes
     *
     * @param seconds
     * @return Double
     */
    public static String convertSecondsToMinutes(long seconds) {
        String result = DateUtils.formatElapsedTime(seconds);
        if (result.length() > 5) {
            return result + " hours";
        } else {
            return result + " mins";
        }
    }

    /**
     * Convert Minutes in HH hours: MM min
     *
     * @param time
     * @return
     */
    public static String convertMinutesToHourMinutes(double time) {
        int hours = (int) (time / 60);
        int minutes = (int) (time % 60);
        String result;
        if (hours > 0) {
            result = hours + " hours: " + minutes + " min";
        } else {
            result = minutes + " min";

        }
        return result;
    }

    /**
     * Convert LatLng to Location
     *
     * @param latLng
     * @return
     */
    public static Location convertLatLngToLocation(LatLng latLng) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    /**
     * Convert CoOrdintates to Location
     *
     * @param latitude
     * @param longitude
     * @return
     */
    public static Location convertCoOrdinationToLocation(double latitude, double longitude) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    /**
     * Parse Through Text File
     * Make LatLng WayPoint ArrayList
     *
     * @param context
     * @param fileName
     * @return
     */
    public static ArrayList<LatLng> readCordinatesFromFile(Context context, String fileName) {
        ArrayList<LatLng> result = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));

            result.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] coordinate = line.split(",");
                if (coordinate.length == 2) {
                    double latitude = Double.parseDouble(coordinate[0]);
                    double longitude = Double.parseDouble(coordinate[1]);
                    result.add(new LatLng(latitude, longitude));
                }
            }
            reader.close();
        } catch (Exception e) {
            Toast.makeText(context, "File not found!", Toast.LENGTH_SHORT).show();
        }

        return result;
    }

}
