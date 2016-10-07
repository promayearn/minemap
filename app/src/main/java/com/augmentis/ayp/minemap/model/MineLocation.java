package com.augmentis.ayp.minemap.model;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Chayanit on 9/30/2016.
 */

public class MineLocation {

    private double latitude;
    private double longitude;
    private int type;

    public MineLocation() {

        latitude = 0;
        longitude = 0;
        type = 1;

    }

    //ส่ง Instance อื่นๆ
    private static MineLocation instance;
    private Context context;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public File getLocPicFile(LocationItem locationItem) {

        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (externalFilesDir == null) {
            return null;
        }
        return new File(externalFilesDir, locationItem.getLoc_pic());
    }

}
