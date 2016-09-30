package com.augmentis.ayp.minemap.model;

/**
 * Created by Chayanit on 9/30/2016.
 */

public class MineLocation {

    private double latitude;
    private double longitude;

    public MineLocation() {

        latitude = 0;
        longitude = 0;

    }

    //ส่ง Instance อื่นๆ
    private static MineLocation instance;

    public static MineLocation getInstance() {
        if (instance == null) {
            instance = new MineLocation();
        }
        return instance;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
