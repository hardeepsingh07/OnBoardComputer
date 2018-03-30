package com.example.hardeepsingh.onboardcomputer.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.io.Serializable;
import com.example.hardeepsingh.onboardcomputer.BR;

/**
 * Building Data
 *
 * @author Hardeep Singh (hardeepsingh@cpp.edu)
 * December 28,2018
 */
public class Building extends BaseObservable implements Serializable {

    private String id;
    private String name;
    private String number;
    private String fullName;
    private String description;
    private String surroundings;
    private double latitude;
    private double longitude;

    public Building() {
    }

    public Building(String id, String name, String surroundings, double latitude, double longitude) {
        this.id = id;
        this.fullName = name;
        this.surroundings = surroundings;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void parseName() {
        String[] split = fullName.split(":");
        if (split.length > 0) {
            for (int i = 0; i < split.length; i++) {
                this.name = split[0].trim();
                this.description = split[1].trim();
                this.number = split[0].trim().split("\\s++")[1].trim();
            }
        }
    }

    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        notifyPropertyChanged(BR.fullName);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
    }

    @Bindable
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        notifyPropertyChanged(BR.latitude);
    }

    @Bindable
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        notifyPropertyChanged(BR.longitude);
    }

    @Bindable
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
        notifyPropertyChanged(BR.number);
    }

    @Bindable
    public String getSurroundings() {
        return surroundings;
    }

    public void setSurroundings(String surroundings) {
        this.surroundings = surroundings;
        notifyPropertyChanged(BR.surroundings);
    }

    @Override
    public String toString() {
        return "Number: " + number + " | Name: " + name + " | Description: " + description +
                " | Surroundings: " + surroundings + " | (Lat: " + latitude + ", Long: " + longitude + ")";
    }
}
