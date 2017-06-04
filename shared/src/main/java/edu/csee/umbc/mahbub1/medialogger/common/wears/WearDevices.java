package com.example.android.wearable.medialogger.common.wears;

/**
 * Created by mahbub on 4/20/17.
 */

public class WearDevices {
    private String wearDevicName;
    private String wearsManufacturer;
    private String id;

    public WearDevices(String wearDevicName, String id) {
        this.wearDevicName = wearDevicName;
        this.id = id;
    }

    public String getWearDevicName() {
        return wearDevicName;
    }

    public void setWearDevicName(String wearDevicName) {
        this.wearDevicName = wearDevicName;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WearDevices() {
    }
}
