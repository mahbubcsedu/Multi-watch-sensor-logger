package edu.csee.umbc.mahbub1.medialogger.common.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Created by mahbub on 4/20/17.
 */

public class PreferenceData {
    private int sensor_frequency;
    private boolean storageLocationIsWatch =false;

    public PreferenceData(int sensor_frequency, boolean storageLocation) {
        this.sensor_frequency = sensor_frequency;
        this.storageLocationIsWatch = storageLocation;
    }

    public PreferenceData() {
    }

    public int getSensor_frequency() {
        return sensor_frequency;
    }

    public void setSensor_frequency(int sensor_frequency) {
        this.sensor_frequency = sensor_frequency;
    }

    public boolean isStorageLocationIsWatch() {
        return storageLocationIsWatch;
    }

    public void setStorageLocationIsWatch(boolean storageLocation) {
        this.storageLocationIsWatch = storageLocation;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return toJson();
    }

    @JsonIgnore
    public String toJson() {
        String jsonData = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            jsonData = mapper.writeValueAsString(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return jsonData;
    }

    public static PreferenceData fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            PreferenceData preferenceData = mapper.readValue(json, PreferenceData.class);
            return preferenceData;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
